package pe.facturacion.shared.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monto final en una moneda, siempre con 2 decimales y redondeo HALF_UP.
 * Los cálculos intermedios de impuestos deben hacerse con {@link BigDecimal} sin redondear
 * y convertirse a {@code Dinero} solo al cerrar cada importe.
 */
public record Dinero(BigDecimal monto, Moneda moneda) {

	public static final int ESCALA = 2;
	public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

	public Dinero {
		Objects.requireNonNull(monto, "monto");
		Objects.requireNonNull(moneda, "moneda");
		monto = monto.setScale(ESCALA, REDONDEO);
	}

	public static Dinero de(BigDecimal monto, Moneda moneda) {
		return new Dinero(monto, moneda);
	}

	public static Dinero de(String monto, Moneda moneda) {
		return new Dinero(new BigDecimal(monto), moneda);
	}

	public static Dinero cero(Moneda moneda) {
		return new Dinero(BigDecimal.ZERO, moneda);
	}

	public Dinero sumar(Dinero otro) {
		validarMisma(otro);
		return new Dinero(monto.add(otro.monto), moneda);
	}

	public Dinero restar(Dinero otro) {
		validarMisma(otro);
		return new Dinero(monto.subtract(otro.monto), moneda);
	}

	public Dinero multiplicar(BigDecimal factor) {
		Objects.requireNonNull(factor, "factor");
		return new Dinero(monto.multiply(factor), moneda);
	}

	public boolean esCero() {
		return monto.signum() == 0;
	}

	public boolean esNegativo() {
		return monto.signum() < 0;
	}

	public boolean esMayorQue(Dinero otro) {
		validarMisma(otro);
		return monto.compareTo(otro.monto) > 0;
	}

	private void validarMisma(Dinero otro) {
		Objects.requireNonNull(otro, "otro");
		if (otro.moneda != moneda) {
			throw new IllegalArgumentException(
					"No se pueden operar montos en %s y %s".formatted(moneda, otro.moneda));
		}
	}

	@Override
	public String toString() {
		return moneda.simbolo() + " " + monto.toPlainString();
	}

}
