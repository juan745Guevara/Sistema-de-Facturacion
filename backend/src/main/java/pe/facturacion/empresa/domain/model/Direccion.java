package pe.facturacion.empresa.domain.model;

import java.util.Locale;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Textos;

/**
 * Domicilio fiscal del emisor tal como lo pide UBL: ubigeo INEI de 6 dígitos y código de establecimiento anexo
 * de SUNAT ({@code 0000} para el domicilio fiscal).
 */
public record Direccion(
		String direccion,
		String ubigeo,
		String departamento,
		String provincia,
		String distrito,
		String codigoPais,
		String codigoEstablecimiento) {

	public static final String PERU = "PE";
	public static final String DOMICILIO_FISCAL = "0000";

	public Direccion {
		direccion = Textos.obligatorio(direccion, "dirección", 200);
		ubigeo = validarFormato(ubigeo, "\\d{6}", "ubigeo", "6 dígitos");
		departamento = Textos.obligatorio(departamento, "departamento", 60);
		provincia = Textos.obligatorio(provincia, "provincia", 60);
		distrito = Textos.obligatorio(distrito, "distrito", 60);
		codigoPais = codigoPais == null || codigoPais.isBlank() ? PERU : codigoPais.trim().toUpperCase(Locale.ROOT);
		codigoPais = validarFormato(codigoPais, "[A-Z]{2}", "código de país", "2 letras");
		codigoEstablecimiento = codigoEstablecimiento == null || codigoEstablecimiento.isBlank() ? DOMICILIO_FISCAL
				: codigoEstablecimiento.trim();
		codigoEstablecimiento = validarFormato(codigoEstablecimiento, "\\d{4}", "código de establecimiento",
				"4 dígitos");
	}

	private static String validarFormato(String valor, String patron, String campo, String formato) {
		String limpio = valor == null ? "" : valor.trim();
		if (!limpio.matches(patron)) {
			throw DominioException.reglaNegocio("formato-invalido",
					"El %s debe tener %s".formatted(campo, formato));
		}
		return limpio;
	}

}
