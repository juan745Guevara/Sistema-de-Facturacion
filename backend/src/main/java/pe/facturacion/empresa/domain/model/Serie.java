package pe.facturacion.empresa.domain.model;

import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

/** Serie de numeración. {@code correlativo} es el último número usado (0 si aún no se emitió). */
public record Serie(Long id, TipoComprobante tipo, String serie, int correlativo, boolean activa) {

	public Serie {
		Objects.requireNonNull(tipo, "tipo");
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (!serie.matches("[A-Z0-9]{4}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie debe tener 4 letras o dígitos");
		}
		String prefijos = prefijosPermitidos(tipo);
		if (!prefijos.isEmpty() && prefijos.indexOf(serie.charAt(0)) < 0) {
			throw DominioException.reglaNegocio("serie-invalida",
					"La serie de %s debe empezar con %s".formatted(tipo.descripcion(), String.join(" o ", prefijos.split(""))));
		}
		if (correlativo < 0 || correlativo > 99_999_999) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo debe estar entre 0 y 99999999");
		}
	}

	public static Serie nueva(TipoComprobante tipo, String serie, int ultimoCorrelativo) {
		return new Serie(null, tipo, serie, ultimoCorrelativo, true);
	}

	public Serie siguiente() {
		if (correlativo == 99_999_999) {
			throw DominioException.reglaNegocio("serie-agotada", "La serie %s llegó al último número".formatted(serie));
		}
		return new Serie(id, tipo, serie, correlativo + 1, activa);
	}

	public Serie conActivacion(boolean activar) {
		return new Serie(id, tipo, serie, correlativo, activar);
	}

	/** Las notas de una boleta usan series B y las de una factura, series F. */
	public boolean paraBoletas() {
		return serie.charAt(0) == 'B';
	}

	private static String prefijosPermitidos(TipoComprobante tipo) {
		return switch (tipo) {
			case FACTURA -> "F";
			case BOLETA -> "B";
			case NOTA_CREDITO, NOTA_DEBITO -> "FB";
			case GUIA_REMISION -> "T";
			default -> "";
		};
	}

}
