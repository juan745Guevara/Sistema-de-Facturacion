package pe.facturacion.sunat.application.port.in;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;

public interface ConsultarDocumentosUseCase {

	DocumentoElectronicoDto obtener(TipoComprobante tipo, String serie, int correlativo);

	Pagina<DocumentoElectronicoDto> buscar(FiltroDocumentos filtro, ConsultaPaginada consulta);

	/** XML firmado tal como se envió. */
	Archivo xml(TipoComprobante tipo, String serie, int correlativo);

	/** Constancia de recepción (zip) devuelta por SUNAT. */
	Archivo cdr(TipoComprobante tipo, String serie, int correlativo);

	record Archivo(String nombre, byte[] contenido) {
	}

}
