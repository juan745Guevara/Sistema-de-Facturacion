package pe.facturacion.shared.domain.model.sunat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Impuesto al consumo de bolsas de plástico (Ley 30884). El monto por bolsa sube cada año
 * hasta 2023 y desde entonces se mantiene en S/ 0.50.
 */
public final class Icbper {

	public static final String CODIGO_TRIBUTO = "7152";

	private Icbper() {
	}

	public static BigDecimal montoPorBolsa(LocalDate fecha) {
		int anio = fecha.getYear();
		if (anio < 2019) {
			return BigDecimal.ZERO.setScale(2);
		}
		return switch (anio) {
			case 2019 -> new BigDecimal("0.10");
			case 2020 -> new BigDecimal("0.20");
			case 2021 -> new BigDecimal("0.30");
			case 2022 -> new BigDecimal("0.40");
			default -> new BigDecimal("0.50");
		};
	}

}
