package pe.facturacion.sunat.application.port.in;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;

public interface EnviarDocumentoUseCase {

	/**
	 * Genera y firma el XML si aún no existe (si existe, reenvía el mismo, porque su hash ya pudo imprimirse) y lo
	 * envía a SUNAT. Un fallo de comunicación deja el documento PENDIENTE para reintentar; un documento en estado
	 * definitivo se devuelve sin reenviar.
	 */
	DocumentoElectronicoDto enviar(TipoComprobante tipo, String serie, int correlativo);

}
