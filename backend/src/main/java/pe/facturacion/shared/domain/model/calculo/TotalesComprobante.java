package pe.facturacion.shared.domain.model.calculo;

import java.math.BigDecimal;

/**
 * Totales de cabecera. {@code gravadas} ya descuenta el descuento global;
 * {@code valorVenta} es la suma de bases cobradas y {@code total} lo que paga el cliente.
 */
public record TotalesComprobante(
		BigDecimal gravadas,
		BigDecimal gravadasIvap,
		BigDecimal exoneradas,
		BigDecimal inafectas,
		BigDecimal exportacion,
		BigDecimal gratuitas,
		BigDecimal descuentoGlobal,
		BigDecimal factorDescuentoGlobal,
		BigDecimal igv,
		BigDecimal ivap,
		BigDecimal igvGratuitas,
		BigDecimal icbper,
		BigDecimal valorVenta,
		BigDecimal total) {

	public BigDecimal impuestos() {
		return igv.add(ivap).add(icbper);
	}

	/** Base gravada antes del descuento global, sobre la que se calcula el factor. */
	public BigDecimal gravadasAntesDeDescuento() {
		return gravadas.add(descuentoGlobal);
	}

}
