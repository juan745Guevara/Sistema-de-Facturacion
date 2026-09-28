package pe.facturacion.sunat.application.usecase;

import java.time.Clock;

import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.application.port.out.EmisorPort;
import pe.facturacion.sunat.application.port.out.EventosPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort.XmlFirmado;
import pe.facturacion.sunat.application.port.out.GeneradorXmlPort;
import pe.facturacion.sunat.application.port.out.ServicioSunatPort;
import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;
import pe.facturacion.sunat.domain.model.Emisor;
import pe.facturacion.sunat.domain.model.RespuestaSunat;

/**
 * La llamada a SUNAT queda fuera de toda transacción: puede tardar decenas de segundos y no debe retener una
 * conexión a la base de datos ni bloquear filas.
 */
public class EnviarDocumentoService implements EnviarDocumentoUseCase {

	private final DocumentoElectronicoRepositoryPort documentos;
	private final EmisorPort emisores;
	private final GeneradorXmlPort generador;
	private final FirmaDigitalPort firma;
	private final ServicioSunatPort sunat;
	private final EventosPort eventos;
	private final Transacciones transacciones;
	private final Clock reloj;

	public EnviarDocumentoService(DocumentoElectronicoRepositoryPort documentos, EmisorPort emisores,
			GeneradorXmlPort generador, FirmaDigitalPort firma, ServicioSunatPort sunat, EventosPort eventos,
			Transacciones transacciones, Clock reloj) {
		this.documentos = documentos;
		this.emisores = emisores;
		this.generador = generador;
		this.firma = firma;
		this.sunat = sunat;
		this.eventos = eventos;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public DocumentoElectronicoDto enviar(TipoComprobante tipo, String serie, int correlativo) {
		DocumentoElectronico documento = Documentos.obtener(documentos, tipo, serie, correlativo);
		if (documento.estado().definitivo()) {
			return Documentos.aDto(documento);
		}
		if (documento.estado() == EstadoSunat.EN_PROCESO) {
			throw DominioException.conflicto("documento-en-proceso",
					"El documento %s está en un resumen o baja pendiente de respuesta".formatted(documento.numero()));
		}
		Emisor emisor = emisores.emisor();
		if (!documento.firmado()) {
			documento = firmar(documento, emisor);
		}

		DocumentoElectronico respondido;
		try {
			RespuestaSunat respuesta = sunat.enviarComprobante(emisor.ruc(), documento.nombreArchivo(emisor.ruc()),
					documento.xmlFirmado());
			respondido = documento.conRespuesta(respuesta, reloj.instant());
		} catch (ErrorComunicacionSunat e) {
			respondido = documento.conErrorComunicacion(e, reloj.instant());
		}
		return Documentos.aDto(guardar(documento, respondido));
	}

	private DocumentoElectronico firmar(DocumentoElectronico documento, Emisor emisor) {
		ComprobanteElectronico datos = documentos
				.datos(documento.tipo(), documento.serie(), documento.correlativo())
				.orElseThrow(() -> new RecursoNoEncontradoException("Contenido del documento", documento.numero()));
		XmlFirmado firmado = firma.firmar(generador.generar(datos, emisor));
		DocumentoElectronico conFirma = documento.conXmlFirmado(firmado.xml(), firmado.hash());
		return transacciones.ejecutar(() -> documentos.actualizar(conFirma));
	}

	private DocumentoElectronico guardar(DocumentoElectronico anterior, DocumentoElectronico nuevo) {
		return transacciones.ejecutar(() -> {
			DocumentoElectronico guardado = documentos.actualizar(nuevo);
			if (anterior.estado() != guardado.estado()) {
				eventos.publicar(new EstadoDocumentoCambiado(guardado.tipo(), guardado.serie(),
						guardado.correlativo(), anterior.estado(), guardado.estado(), guardado.codigoRespuesta(),
						guardado.mensaje()));
			}
			return guardado;
		});
	}

}
