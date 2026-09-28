package pe.facturacion.shared.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ItemCalculo;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv.Tributo;

/**
 * Calcula líneas y totales de un comprobante con las reglas del sistema anterior, pero en
 * {@link BigDecimal} y con redondeo HALF_UP a 2 decimales por línea y por total.
 */
public final class CalculadoraComprobante {

	public static final BigDecimal TASA_IVAP = new BigDecimal("4");

	private static final int ESCALA = 2;
	private static final int ESCALA_UNITARIO = 10;
	private static final int ESCALA_FACTOR = 5;
	private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
	private static final BigDecimal CIEN = BigDecimal.valueOf(100);
	private static final BigDecimal CERO = BigDecimal.ZERO.setScale(ESCALA);

	private CalculadoraComprobante() {
	}

	public static ResultadoCalculo calcular(List<ItemCalculo> items, BigDecimal porcentajeIgv,
			DescuentoGlobal descuento, BigDecimal icbperPorBolsa) {
		Objects.requireNonNull(porcentajeIgv, "porcentajeIgv");
		Objects.requireNonNull(icbperPorBolsa, "icbperPorBolsa");
		if (items == null || items.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "El comprobante debe tener al menos una línea");
		}
		List<LineaCalculada> lineas = new ArrayList<>(items.size());
		for (ItemCalculo item : items) {
			lineas.add(calcularLinea(item, porcentajeIgv, icbperPorBolsa));
		}
		return new ResultadoCalculo(lineas,
				totalizar(lineas, porcentajeIgv, descuento == null ? DescuentoGlobal.ninguno() : descuento));
	}

	public static BigDecimal tasaPara(TipoAfectacionIgv afectacion, BigDecimal porcentajeIgv) {
		if (afectacion.tributo() == Tributo.IVAP) {
			return TASA_IVAP;
		}
		return afectacion.gravado() ? porcentajeIgv : BigDecimal.ZERO;
	}

	private static LineaCalculada calcularLinea(ItemCalculo item, BigDecimal porcentajeIgv, BigDecimal icbperPorBolsa) {
		if (item.cantidad() == null || item.cantidad().signum() <= 0) {
			throw DominioException.reglaNegocio("cantidad-invalida", "La cantidad debe ser mayor que cero");
		}
		if (item.precioUnitario() == null || item.precioUnitario().signum() <= 0) {
			throw DominioException.reglaNegocio("precio-invalido", "El precio unitario debe ser mayor que cero");
		}
		TipoAfectacionIgv afectacion = item.afectacion();
		BigDecimal tasa = tasaPara(afectacion, porcentajeIgv);
		BigDecimal valorUnitario = item.precioUnitario()
				.divide(BigDecimal.ONE.add(tasa.divide(CIEN)), ESCALA_UNITARIO, REDONDEO);
		BigDecimal bruto = valorUnitario.multiply(item.cantidad());
		BigDecimal descuento = item.descuento().setScale(ESCALA, REDONDEO);
		if (descuento.signum() < 0 || descuento.compareTo(bruto) > 0
				|| (afectacion.gratuito() && descuento.signum() > 0)) {
			throw DominioException.reglaNegocio("descuento-invalido",
					"El descuento de la línea debe estar entre 0 y su valor de venta, y no aplica a gratuitas");
		}
		BigDecimal valorVenta = bruto.subtract(descuento).setScale(ESCALA, REDONDEO);
		BigDecimal impuesto = porcentaje(valorVenta, tasa);
		BigDecimal bolsa = icbperPorBolsa.setScale(ESCALA, REDONDEO);
		BigDecimal icbper = item.icbper() ? item.cantidad().multiply(bolsa).setScale(ESCALA, REDONDEO) : CERO;
		return new LineaCalculada(item.cantidad(), afectacion, item.precioUnitario().setScale(ESCALA_UNITARIO, REDONDEO)
				.stripTrailingZeros(), valorUnitario.stripTrailingZeros(), tasa, descuento, valorVenta, impuesto, icbper,
				item.icbper() ? bolsa : CERO);
	}

	private static TotalesComprobante totalizar(List<LineaCalculada> lineas, BigDecimal porcentajeIgv,
			DescuentoGlobal descuento) {
		BigDecimal gravadas = CERO;
		BigDecimal gravadasIvap = CERO;
		BigDecimal exoneradas = CERO;
		BigDecimal inafectas = CERO;
		BigDecimal exportacion = CERO;
		BigDecimal gratuitas = CERO;
		BigDecimal igvLineas = CERO;
		BigDecimal ivap = CERO;
		BigDecimal igvGratuitas = CERO;
		BigDecimal icbper = CERO;
		for (LineaCalculada linea : lineas) {
			icbper = icbper.add(linea.icbper());
			if (linea.afectacion().gratuito()) {
				gratuitas = gratuitas.add(linea.valorVenta());
				igvGratuitas = igvGratuitas.add(linea.impuesto());
				continue;
			}
			switch (linea.afectacion().tributo()) {
				case IGV -> {
					gravadas = gravadas.add(linea.valorVenta());
					igvLineas = igvLineas.add(linea.impuesto());
				}
				case IVAP -> {
					gravadasIvap = gravadasIvap.add(linea.valorVenta());
					ivap = ivap.add(linea.impuesto());
				}
				case EXONERADO -> exoneradas = exoneradas.add(linea.valorVenta());
				case INAFECTO -> inafectas = inafectas.add(linea.valorVenta());
				case EXPORTACION -> exportacion = exportacion.add(linea.valorVenta());
				case GRATUITO -> gratuitas = gratuitas.add(linea.valorVenta());
			}
		}

		BigDecimal montoDescuento = CERO;
		BigDecimal factor = BigDecimal.ZERO.setScale(ESCALA_FACTOR);
		BigDecimal igv = igvLineas;
		if (descuento.aplica()) {
			if (gravadas.signum() == 0) {
				throw DominioException.reglaNegocio("descuento-sin-gravadas",
						"El descuento global solo se aplica si hay operaciones gravadas");
			}
			montoDescuento = descuento.tipo() == DescuentoGlobal.Tipo.PORCENTAJE
					? porcentaje(gravadas, descuento.valor())
					: descuento.valor().setScale(ESCALA, REDONDEO);
			if (montoDescuento.signum() < 0 || montoDescuento.compareTo(gravadas) > 0) {
				throw DominioException.reglaNegocio("descuento-invalido",
						"El descuento global debe estar entre 0 y el total de operaciones gravadas");
			}
			factor = montoDescuento.divide(gravadas, ESCALA_FACTOR, REDONDEO);
			gravadas = gravadas.subtract(montoDescuento);
			igv = porcentaje(gravadas, porcentajeIgv);
		}

		BigDecimal valorVenta = gravadas.add(gravadasIvap).add(exoneradas).add(inafectas).add(exportacion);
		BigDecimal total = valorVenta.add(igv).add(ivap).add(icbper);
		return new TotalesComprobante(gravadas, gravadasIvap, exoneradas, inafectas, exportacion, gratuitas,
				montoDescuento, factor, igv, ivap, igvGratuitas, icbper, valorVenta, total);
	}

	private static BigDecimal porcentaje(BigDecimal base, BigDecimal tasa) {
		return base.multiply(tasa).divide(CIEN, ESCALA, REDONDEO);
	}

}
