package pe.facturacion.shared.domain.model.calculo;

import java.math.BigDecimal;

/** Descuento global que afecta la base imponible del IGV (catálogo 53, código 02). */
public record DescuentoGlobal(Tipo tipo, BigDecimal valor) {

	public enum Tipo {
		MONTO,
		PORCENTAJE
	}

	public DescuentoGlobal {
		tipo = tipo == null ? Tipo.MONTO : tipo;
		valor = valor == null ? BigDecimal.ZERO : valor;
	}

	public static DescuentoGlobal ninguno() {
		return new DescuentoGlobal(Tipo.MONTO, BigDecimal.ZERO);
	}

	public boolean aplica() {
		return valor.signum() != 0;
	}

}
