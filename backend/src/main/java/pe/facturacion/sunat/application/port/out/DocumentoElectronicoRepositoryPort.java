package pe.facturacion.sunat.application.port.out;

import java.util.Optional;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;

public interface DocumentoElectronicoRepositoryPort {

	boolean existe(TipoComprobante tipo, String serie, int correlativo);

	Optional<DocumentoElectronico> buscar(TipoComprobante tipo, String serie, int correlativo);

	/** Contenido del comprobante con el que se genera el XML. */
	Optional<ComprobanteElectronico> datos(TipoComprobante tipo, String serie, int correlativo);

	Pagina<DocumentoElectronico> buscar(FiltroDocumentos filtro, ConsultaPaginada consulta);

	DocumentoElectronico registrar(DocumentoElectronico documento, ComprobanteElectronico datos);

	DocumentoElectronico actualizar(DocumentoElectronico documento);

}
