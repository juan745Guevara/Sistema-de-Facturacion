package pe.facturacion.sunat.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.dto.LoteDto;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;
import pe.facturacion.sunat.application.port.in.GestionarLotesUseCase;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.application.port.out.EmisorPort;
import pe.facturacion.sunat.application.port.out.EventosPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort.XmlFirmado;
import pe.facturacion.sunat.application.port.out.GeneradorXmlLotePort;
import pe.facturacion.sunat.application.port.out.LoteRepositoryPort;
import pe.facturacion.sunat.application.port.out.ServicioSunatPort;
import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;
import pe.facturacion.sunat.domain.model.Emisor;
import pe.facturacion.sunat.domain.model.LineaLote;
import pe.facturacion.sunat.domain.model.LoteSunat;
import pe.facturacion.sunat.domain.model.RespuestaSunat;
import pe.facturacion.sunat.domain.model.TipoLote;

public class GestionarLotesService implements GestionarLotesUseCase {

	private final DocumentoElectronicoRepositoryPort documentos;
	private final LoteRepositoryPort lotes;
	private final EmisorPort emisores;
	private final GeneradorXmlLotePort generador;
	private final FirmaDigitalPort firma;
	private final ServicioSunatPort sunat;
	private final EventosPort eventos;
	private final Transacciones transacciones;
	private final Clock reloj;

	public GestionarLotesService(DocumentoElectronicoRepositoryPort documentos, LoteRepositoryPort lotes,
			EmisorPort emisores, GeneradorXmlLotePort generador, FirmaDigitalPort firma, ServicioSunatPort sunat,
			EventosPort eventos, Transacciones transacciones, Clock reloj) {
		this.documentos = documentos;
		this.lotes = lotes;
		this.emisores = emisores;
		this.generador = generador;
		this.firma = firma;
		this.sunat = sunat;
		this.eventos = eventos;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public List<DocumentoPendiente> previsualizarResumen(LocalDate fecha) {
		return documentos.listar(TipoComprobante.BOLETA, fecha, EstadoSunat.PENDIENTE).stream()
				.map(d -> {
					ComprobanteElectronico datos = documentos.datos(d.tipo(), d.serie(), d.correlativo()).orElse(null);
					String cliente = datos == null ? "" : datos.receptor().nombre();
					String total = datos == null ? "0.00" : datos.totales().total().toPlainString();
					return new DocumentoPendiente(d.tipo(), d.serie(), d.correlativo(), cliente, total);
				})
				.toList();
	}

	@Override
	public LoteDto generarResumen(LocalDate fecha) {
		LoteSunat lote = transacciones.ejecutar(() -> armarResumen(fecha));
		return enviar(lote);
	}

	@Override
	public LoteDto darBaja(TipoComprobante tipo, String serie, int correlativo, String motivo) {
		LoteSunat lote = transacciones.ejecutar(() -> armarBaja(tipo, serie, correlativo, motivo));
		return enviar(lote);
	}

	@Override
	public LoteDto consultarTicket(Long loteId) {
		LoteSunat lote = lotes.buscarPorId(loteId).orElseThrow(() -> new RecursoNoEncontradoException("Lote SUNAT", loteId));
		if (lote.estado() != EstadoSunat.EN_PROCESO || lote.ticket() == null) {
			return aDto(lote);
		}
		Emisor emisor = emisores.emisor();
		RespuestaSunat respuesta;
		try {
			respuesta = sunat.consultarTicket(emisor.ruc(), lote.ticket());
		} catch (ErrorComunicacionSunat e) {
			return aDto(transacciones.ejecutar(() -> lotes.guardar(lote.conErrorComunicacion(e, reloj.instant()))));
		}
		if (respuesta == null) {
			return aDto(lote);
		}
		LoteSunat resuelto = lote.conRespuesta(respuesta, reloj.instant());
		return aDto(transacciones.ejecutar(() -> aplicarResolucion(lote, resuelto)));
	}

	@Override
	public Pagina<LoteDto> buscar(TipoLote tipo, ConsultaPaginada consulta) {
		return lotes.buscar(tipo, consulta).map(GestionarLotesService::aDto);
	}

	@Override
	public LoteDto obtener(Long id) {
		return aDto(lotes.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Lote SUNAT", id)));
	}

	private LoteSunat armarResumen(LocalDate fecha) {
		List<DocumentoElectronico> boletas = documentos.listar(TipoComprobante.BOLETA, fecha, EstadoSunat.PENDIENTE);
		if (boletas.isEmpty()) {
			throw DominioException.reglaNegocio("resumen-vacio",
					"No hay boletas pendientes para el " + fecha);
		}
		List<LineaLote> lineas = new ArrayList<>();
		for (DocumentoElectronico documento : boletas) {
			ComprobanteElectronico datos = documentos.datos(documento.tipo(), documento.serie(), documento.correlativo())
					.orElseThrow(() -> new RecursoNoEncontradoException("Contenido del documento", documento.numero()));
			lineas.add(new LineaLote(documento.tipo(), documento.serie(), documento.correlativo(), LineaLote.ADICION,
					datos.receptor().tipoDocumento(), datos.receptor().numeroDocumento(), datos.moneda(),
					datos.totales(), null));
			documentos.actualizar(documento.conEstado(EstadoSunat.EN_PROCESO, null, "Incluido en resumen diario"));
		}
		LocalDate hoy = LocalDate.now(reloj);
		int correlativo = lotes.siguienteCorrelativo(TipoLote.RESUMEN_DIARIO, hoy);
		return lotes.guardar(LoteSunat.nuevo(TipoLote.RESUMEN_DIARIO, fecha, hoy, correlativo, lineas));
	}

	private LoteSunat armarBaja(TipoComprobante tipo, String serie, int correlativo, String motivo) {
		if (tipo != TipoComprobante.FACTURA && tipo != TipoComprobante.NOTA_CREDITO
				&& tipo != TipoComprobante.NOTA_DEBITO) {
			throw DominioException.reglaNegocio("baja-invalida",
					"La comunicación de baja solo aplica a facturas y notas");
		}
		DocumentoElectronico documento = Documentos.obtener(documentos, tipo, serie, correlativo);
		if (!documento.estado().aceptado()) {
			throw DominioException.reglaNegocio("baja-sin-aceptacion",
					"Solo se da de baja un comprobante aceptado por SUNAT");
		}
		if (motivo == null || motivo.isBlank()) {
			throw DominioException.reglaNegocio("motivo-obligatorio", "La baja exige un motivo");
		}
		documentos.actualizar(documento.conEstado(EstadoSunat.EN_PROCESO, null, "Incluido en comunicación de baja"));
		LocalDate hoy = LocalDate.now(reloj);
		int numero = lotes.siguienteCorrelativo(TipoLote.COMUNICACION_BAJA, hoy);
		LineaLote linea = new LineaLote(tipo, serie, correlativo, LineaLote.BAJA, null, null, null, null, motivo.trim());
		return lotes.guardar(LoteSunat.nuevo(TipoLote.COMUNICACION_BAJA, documento.fechaEmision(), hoy, numero,
				List.of(linea)));
	}

	private LoteDto enviar(LoteSunat lote) {
		Emisor emisor = emisores.emisor();
		final LoteSunat listo;
		if (lote.firmado()) {
			listo = lote;
		} else {
			XmlFirmado xml = firma.firmar(generador.generar(lote, emisor));
			listo = transacciones.ejecutar(() -> lotes.guardar(lote.conXmlFirmado(xml.xml(), xml.hash())));
		}
		try {
			String ticket = sunat.enviarResumen(emisor.ruc(), listo.nombreArchivo(emisor.ruc()), listo.xmlFirmado());
			return aDto(transacciones.ejecutar(() -> lotes.guardar(listo.conTicket(ticket, reloj.instant()))));
		} catch (DominioException e) {
			return aDto(listo);
		}
	}

	private LoteSunat aplicarResolucion(LoteSunat anterior, LoteSunat resuelto) {
		LoteSunat guardado = lotes.guardar(resuelto);
		EstadoSunat destinoDocumentos = destinoDocumentos(guardado);
		for (LineaLote linea : guardado.lineas()) {
			documentos.buscar(linea.tipo(), linea.serie(), linea.correlativo()).ifPresent(documento -> {
				EstadoSunat previo = documento.estado();
				DocumentoElectronico actualizado = documentos.actualizar(documento.conEstado(destinoDocumentos,
						guardado.codigoRespuesta(), guardado.mensaje()));
				if (previo != actualizado.estado()) {
					eventos.publicar(new EstadoDocumentoCambiado(actualizado.tipo(), actualizado.serie(),
							actualizado.correlativo(), previo, actualizado.estado(), actualizado.codigoRespuesta(),
							actualizado.mensaje()));
				}
			});
		}
		return guardado;
	}

	private static EstadoSunat destinoDocumentos(LoteSunat lote) {
		if (lote.estado() == EstadoSunat.RECHAZADO) {
			return lote.tipo() == TipoLote.COMUNICACION_BAJA ? EstadoSunat.ACEPTADO : EstadoSunat.PENDIENTE;
		}
		if (lote.tipo() == TipoLote.COMUNICACION_BAJA) {
			return EstadoSunat.ANULADO;
		}
		return lote.estado().aceptado() ? EstadoSunat.ACEPTADO : lote.estado();
	}

	private static LoteDto aDto(LoteSunat lote) {
		return new LoteDto(lote.id(), lote.tipo(), lote.serie(), lote.correlativo(), lote.fechaReferencia(),
				lote.fechaGeneracion(), lote.estado(), lote.ticket(), lote.codigoRespuesta(), lote.mensaje(),
				lote.intentos(), lote.ultimoEnvio(), lote.firmado(), lote.cdr() != null, lote.lineas());
	}

}
