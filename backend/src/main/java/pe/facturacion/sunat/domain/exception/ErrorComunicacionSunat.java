package pe.facturacion.sunat.domain.exception;

import pe.facturacion.shared.domain.exception.DominioException;

/**
 * SUNAT no procesó el documento: falla de red, servicio caído o excepción 0100-1999. El documento sigue pendiente
 * y puede reenviarse tal cual.
 */
public class ErrorComunicacionSunat extends DominioException {

	private final String codigoSunat;

	public ErrorComunicacionSunat(String codigoSunat, String mensaje) {
		super(TipoError.SERVICIO_NO_DISPONIBLE, "sunat-no-disponible", mensaje);
		this.codigoSunat = codigoSunat;
	}

	/** Código de excepción de SUNAT, o {@code null} si ni siquiera hubo respuesta. */
	public String codigoSunat() {
		return codigoSunat;
	}

}
