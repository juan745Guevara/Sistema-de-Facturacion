package pe.facturacion.shared.domain.model;

import pe.facturacion.shared.domain.exception.DominioException;

/** Normalización y validación de textos de los modelos de dominio. */
public final class Textos {

	private Textos() {
	}

	/** Quita espacios de los extremos; devuelve {@code null} si queda vacío. */
	public static String opcional(String valor, String campo, int longitudMaxima) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return limitar(valor.trim(), campo, longitudMaxima);
	}

	public static String obligatorio(String valor, String campo, int longitudMaxima) {
		if (valor == null || valor.isBlank()) {
			throw DominioException.reglaNegocio("campo-obligatorio", "El campo %s es obligatorio".formatted(campo));
		}
		return limitar(valor.trim(), campo, longitudMaxima);
	}

	private static String limitar(String valor, String campo, int longitudMaxima) {
		if (valor.length() > longitudMaxima) {
			throw DominioException.reglaNegocio("campo-demasiado-largo",
					"El campo %s admite hasta %d caracteres".formatted(campo, longitudMaxima));
		}
		return valor;
	}

}
