package pe.facturacion.shared.domain.model.calculo;

import java.math.BigDecimal;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

/**
 * Importes de una línea. {@code valorUnitario} conserva 10 decimales, como admite UBL;
 * el resto está redondeado a 2. En las gratuitas, valor e impuesto son referenciales.
 */
public record LineaCalculada(
		BigDecimal cantidad,
		TipoAfectacionIgv afectacion,
		BigDecimal precioUnitario,
		BigDecimal valorUnitario,
		BigDecimal porcentajeImpuesto,
		BigDecimal descuento,
		BigDecimal valorVenta,
		BigDecimal impuesto,
		BigDecimal icbper,
		BigDecimal icbperPorBolsa) {

	/** Lo que paga el cliente por esta línea. */
	public BigDecimal importe() {
		BigDecimal cobrado = afectacion.gratuito() ? BigDecimal.ZERO : valorVenta.add(impuesto);
		return cobrado.add(icbper);
	}

	public boolean conIcbper() {
		return icbper.signum() > 0;
	}

}
