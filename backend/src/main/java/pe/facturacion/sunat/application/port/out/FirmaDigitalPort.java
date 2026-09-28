package pe.facturacion.sunat.application.port.out;

public interface FirmaDigitalPort {

	/** Firma el XML con el certificado digital del emisor. */
	XmlFirmado firmar(byte[] xml);

	/** {@code hash} es el DigestValue de la firma, que se imprime en la representación impresa. */
	record XmlFirmado(byte[] xml, String hash) {
	}

}
