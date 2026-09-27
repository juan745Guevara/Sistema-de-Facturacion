package pe.facturacion.shared.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import pe.facturacion.shared.domain.model.Dinero;
import pe.facturacion.shared.domain.model.Moneda;

/**
 * Leyenda 1000 de SUNAT: importe total en letras, p. ej. {@code SESENTA Y TRES CON 00/100 SOLES}.
 */
public final class MontoEnLetras {

	private static final long MAXIMO = 999_999_999_999L;

	private static final String[] HASTA_VEINTINUEVE = {
			"", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
			"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO",
			"DIECINUEVE", "VEINTE", "VEINTIUNO", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO",
			"VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE" };

	private static final String[] DECENAS = {
			"", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA" };

	private static final String[] CENTENAS = {
			"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS",
			"SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS" };

	private MontoEnLetras() {
	}

	public static String convertir(Dinero dinero) {
		Objects.requireNonNull(dinero, "dinero");
		return convertir(dinero.monto(), dinero.moneda());
	}

	public static String convertir(BigDecimal monto, Moneda moneda) {
		Objects.requireNonNull(monto, "monto");
		Objects.requireNonNull(moneda, "moneda");
		BigDecimal redondeado = monto.setScale(2, RoundingMode.HALF_UP);
		if (redondeado.signum() < 0) {
			throw new IllegalArgumentException("El monto no puede ser negativo: " + monto);
		}
		long entero = redondeado.longValue();
		if (entero > MAXIMO) {
			throw new IllegalArgumentException("Monto fuera de rango: " + monto);
		}
		int centimos = redondeado.remainder(BigDecimal.ONE).movePointRight(2).intValue();

		String parteEntera;
		if (entero == 0) {
			parteEntera = "CERO";
		} else if (entero == 1) {
			parteEntera = "UN";
		} else {
			parteEntera = enLetras(entero);
		}
		return "%s CON %02d/100 %s".formatted(parteEntera, centimos, moneda.nombreEnLetras());
	}

	private static String enLetras(long numero) {
		if (numero >= 1_000_000) {
			long millones = numero / 1_000_000;
			String prefijo = millones == 1 ? "UN MILLON" : apocopar(enLetras(millones)) + " MILLONES";
			return unir(prefijo, numero % 1_000_000);
		}
		if (numero >= 1000) {
			long miles = numero / 1000;
			String prefijo = miles == 1 ? "MIL" : apocopar(enLetras(miles)) + " MIL";
			return unir(prefijo, numero % 1000);
		}
		return centenas((int) numero);
	}

	private static String unir(String prefijo, long resto) {
		return resto == 0 ? prefijo : prefijo + " " + enLetras(resto);
	}

	private static String centenas(int numero) {
		if (numero == 100) {
			return "CIEN";
		}
		int centena = numero / 100;
		int resto = numero % 100;
		if (centena == 0) {
			return decenas(resto);
		}
		return resto == 0 ? CENTENAS[centena] : CENTENAS[centena] + " " + decenas(resto);
	}

	private static String decenas(int numero) {
		if (numero < 30) {
			return HASTA_VEINTINUEVE[numero];
		}
		int unidad = numero % 10;
		return unidad == 0 ? DECENAS[numero / 10] : DECENAS[numero / 10] + " Y " + HASTA_VEINTINUEVE[unidad];
	}

	/** Delante de MIL y MILLONES: «VEINTIUNO» pasa a «VEINTIUN», «TREINTA Y UNO» a «TREINTA Y UN». */
	private static String apocopar(String texto) {
		return texto.endsWith("UNO") ? texto.substring(0, texto.length() - 1) : texto;
	}

}
