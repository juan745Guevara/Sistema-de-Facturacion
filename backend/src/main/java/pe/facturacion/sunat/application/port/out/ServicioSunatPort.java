package pe.facturacion.sunat.application.port.out;

import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;
import pe.facturacion.sunat.domain.model.RespuestaSunat;

/** Servicio SOAP de facturación electrónica (billService). */
public interface ServicioSunatPort {

	/**
	 * {@code sendBill}: envío síncrono de facturas, boletas y notas.
	 *
	 * @param nombreArchivo nombre sin extensión ({@code RUC-TT-SERIE-CORRELATIVO})
	 * @throws ErrorComunicacionSunat si SUNAT no llegó a procesar el documento
	 */
	RespuestaSunat enviarComprobante(String rucEmisor, String nombreArchivo, byte[] xmlFirmado);

}
