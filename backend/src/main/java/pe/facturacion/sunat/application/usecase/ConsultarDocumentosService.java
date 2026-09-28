package pe.facturacion.sunat.application.usecase;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.application.port.out.EmisorPort;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;

public class ConsultarDocumentosService implements ConsultarDocumentosUseCase {

	private final DocumentoElectronicoRepositoryPort documentos;
	private final EmisorPort emisores;

	public ConsultarDocumentosService(DocumentoElectronicoRepositoryPort documentos, EmisorPort emisores) {
		this.documentos = documentos;
		this.emisores = emisores;
	}

	@Override
	public DocumentoElectronicoDto obtener(TipoComprobante tipo, String serie, int correlativo) {
		return Documentos.aDto(Documentos.obtener(documentos, tipo, serie, correlativo));
	}

	@Override
	public Pagina<DocumentoElectronicoDto> buscar(FiltroDocumentos filtro, ConsultaPaginada consulta) {
		return documentos.buscar(filtro, consulta).map(Documentos::aDto);
	}

	@Override
	public Archivo xml(TipoComprobante tipo, String serie, int correlativo) {
		DocumentoElectronico documento = Documentos.obtener(documentos, tipo, serie, correlativo);
		if (!documento.firmado()) {
			throw new RecursoNoEncontradoException("XML firmado de", documento.numero());
		}
		return new Archivo(documento.nombreArchivo(emisores.emisor().ruc()) + ".xml", documento.xmlFirmado());
	}

	@Override
	public Archivo cdr(TipoComprobante tipo, String serie, int correlativo) {
		DocumentoElectronico documento = Documentos.obtener(documentos, tipo, serie, correlativo);
		if (documento.cdr() == null) {
			throw new RecursoNoEncontradoException("CDR de", documento.numero());
		}
		return new Archivo("R-" + documento.nombreArchivo(emisores.emisor().ruc()) + ".zip", documento.cdr());
	}

}
