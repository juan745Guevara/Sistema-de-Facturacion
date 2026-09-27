package pe.facturacion.shared.domain.model;

import java.util.Arrays;

/** Catálogo 02 de SUNAT (ISO 4217), limitado a las monedas que usa el sistema. */
public enum Moneda {

	PEN("SOLES", "S/"),
	USD("DÓLARES", "US$");

	private final String nombreEnLetras;
	private final String simbolo;

	Moneda(String nombreEnLetras, String simbolo) {
		this.nombreEnLetras = nombreEnLetras;
		this.simbolo = simbolo;
	}

	public String nombreEnLetras() {
		return nombreEnLetras;
	}

	public String simbolo() {
		return simbolo;
	}

	public static Moneda desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(m -> m.name().equalsIgnoreCase(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Moneda no soportada: " + codigo));
	}

}
