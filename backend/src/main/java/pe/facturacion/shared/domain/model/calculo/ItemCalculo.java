package pe.facturacion.shared.domain.model.calculo;

import java.math.BigDecimal;
import java.util.Objects;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

/**
 * Línea a calcular. {@code precioUnitario} incluye los impuestos (en las gratuitas es el precio
 * referencial) y {@code descuento} se aplica sobre el valor de venta, sin impuestos.
 */
public record ItemCalculo(
		BigDecimal cantidad,
		BigDecimal precioUnitario,
		TipoAfectacionIgv afectacion,
		BigDecimal descuento,
		boolean icbper) {

	public ItemCalculo {
		Objects.requireNonNull(afectacion, "afectacion");
		descuento = descuento == null ? BigDecimal.ZERO : descuento;
	}

}
