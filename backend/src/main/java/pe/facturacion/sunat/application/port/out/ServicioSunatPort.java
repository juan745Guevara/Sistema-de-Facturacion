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

	/**
	 * {@code sendSummary}: resumen diario o comunicación de baja. Devuelve el ticket de consulta.
	 *
	 * @param nombreArchivo nombre sin extensión ({@code RUC-RC-YYYYMMDD-N} o {@code RUC-RA-YYYYMMDD-N})
	 */
	String enviarResumen(String rucEmisor, String nombreArchivo, byte[] xmlFirmado);

	/**
	 * {@code getStatus}: consulta el ticket de un resumen o baja.
	 *
	 * @return respuesta con CDR si ya resolvió; {@code null} si sigue en proceso
	 */
	RespuestaSunat consultarTicket(String rucEmisor, String ticket);

}
