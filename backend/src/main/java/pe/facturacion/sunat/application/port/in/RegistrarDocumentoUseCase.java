package pe.facturacion.sunat.application.port.in;

import pe.facturacion.sunat.application.dto.ComprobanteElectronico;

public interface RegistrarDocumentoUseCase {

	/**
	 * Deja el comprobante PENDIENTE de envío. Debe llamarse dentro de la transacción que lo emite, para que no
	 * exista una venta sin su documento electrónico ni al revés.
	 */
	void registrar(ComprobanteElectronico comprobante);

}
