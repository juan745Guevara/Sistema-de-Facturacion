package pe.facturacion.ventas.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

class VentaTest {

	@Test
	void unaFacturaExigeRuc() {
		assertThatThrownBy(() -> venta(TipoComprobante.FACTURA, DocumentoIdentidad.dni("47204426"), totales("50"),
				FormaPago.CONTADO, List.of()))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("factura-sin-ruc");
	}

	@Test
	void unaBoletaAltaExigeDocumento() {
		assertThatThrownBy(() -> venta(TipoComprobante.BOLETA,
				new DocumentoIdentidad(pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad.SIN_DOCUMENTO, "-"),
				totales("700"), FormaPago.CONTADO, List.of()))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("boleta-sin-documento");
	}

	@Test
	void lasCuotasDebenSumarElTotal() {
		assertThatThrownBy(() -> venta(TipoComprobante.NOTA_VENTA, DocumentoIdentidad.dni("47204426"), totales("100"),
				FormaPago.CREDITO, List.of(new Cuota(1, LocalDate.now().plusDays(10), new BigDecimal("40")))))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("cuotas-descuadradas");
	}

	private static Venta venta(TipoComprobante tipo, DocumentoIdentidad documento, TotalesComprobante totales,
			FormaPago pago, List<Cuota> cuotas) {
		LineaCalculada linea = new LineaCalculada(BigDecimal.ONE, TipoAfectacionIgv.GRAVADO_ONEROSO,
				new BigDecimal("118"), new BigDecimal("100"), new BigDecimal("18"), BigDecimal.ZERO, totales.gravadas(),
				totales.igv(), BigDecimal.ZERO, BigDecimal.ZERO);
		return new Venta(null, tipo, tipo == TipoComprobante.FACTURA ? "F001"
				: tipo == TipoComprobante.BOLETA ? "B001" : "N001", 1, LocalDate.now(), LocalTime.NOON, null, Moneda.PEN,
				BigDecimal.ONE, 1L, documento, "Cliente", null,
				List.of(new LineaVenta(1L, "P1", "Producto", "NIU", linea)), totales, pago, cuotas, false, false, null,
				null, false);
	}

	private static TotalesComprobante totales(String total) {
		BigDecimal t = new BigDecimal(total);
		BigDecimal gravadas = t.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);
		BigDecimal igv = t.subtract(gravadas);
		return new TotalesComprobante(gravadas, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, igv, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, gravadas, t);
	}

}
