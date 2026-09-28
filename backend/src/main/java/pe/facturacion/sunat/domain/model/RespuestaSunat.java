package pe.facturacion.sunat.domain.model;

import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;

/**
 * Resultado de un envío procesado por SUNAT: el CDR (si lo hubo) o el código de rechazo. Los errores de
 * comunicación y las excepciones 0100-1999 no llegan aquí: se expresan con
 * {@link pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat}.
 */
public record RespuestaSunat(String codigo, String descripcion, List<String> observaciones, byte[] cdr) {

	public RespuestaSunat {
		Objects.requireNonNull(codigo, "codigo");
		descripcion = descripcion == null ? "" : descripcion;
		observaciones = observaciones == null ? List.of() : List.copyOf(observaciones);
	}

	public static RespuestaSunat aceptada(String descripcion, List<String> observaciones, byte[] cdr) {
		return new RespuestaSunat("0", descripcion, observaciones, cdr);
	}

	public static RespuestaSunat rechazada(String codigo, String descripcion) {
		return new RespuestaSunat(codigo, descripcion, List.of(), null);
	}

	public EstadoSunat estado() {
		int numero = numerico();
		if (numero == 0) {
			return observaciones.isEmpty() ? EstadoSunat.ACEPTADO : EstadoSunat.OBSERVADO;
		}
		if (numero >= 4000) {
			return EstadoSunat.OBSERVADO;
		}
		if (numero >= 2000) {
			return EstadoSunat.RECHAZADO;
		}
		return EstadoSunat.PENDIENTE;
	}

	private int numerico() {
		try {
			return Integer.parseInt(codigo.trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

}
