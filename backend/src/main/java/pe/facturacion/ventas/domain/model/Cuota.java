package pe.facturacion.ventas.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;

public record Cuota(int numero, LocalDate fechaPago, BigDecimal monto) {

	public Cuota {
		if (numero < 1) {
			throw DominioException.reglaNegocio("cuota-invalida", "El número de cuota debe ser mayor que cero");
		}
		Objects.requireNonNull(fechaPago, "fechaPago");
		if (monto == null || monto.signum() <= 0) {
			throw DominioException.reglaNegocio("cuota-invalida", "El monto de cada cuota debe ser mayor que cero");
		}
		monto = monto.setScale(2, RoundingMode.HALF_UP);
	}

}
