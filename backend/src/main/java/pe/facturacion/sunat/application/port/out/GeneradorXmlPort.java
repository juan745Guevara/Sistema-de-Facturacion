package pe.facturacion.sunat.application.port.out;

import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.domain.model.Emisor;

public interface GeneradorXmlPort {

	/** XML UBL 2.1 sin firmar, con el nodo {@code ext:ExtensionContent} vacío donde irá la firma. */
	byte[] generar(ComprobanteElectronico comprobante, Emisor emisor);

}
