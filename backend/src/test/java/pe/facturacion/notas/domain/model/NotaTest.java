package pe.facturacion.notas.domain.model;

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
import pe.facturacion.ventas.domain.model.LineaVenta;

class NotaTest {

	@Test
	void laSerieDeUnaNotaSobreFacturaDebeEmpezarConF() {
		assertThatThrownBy(() -> nota("BC01", TipoComprobante.FACTURA, "F001"))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("serie-invalida");
	}

	@Test
	void noAceptaUnMotivoQueNoEstaEnElCatalogo() {
		assertThatThrownBy(() -> nota("FC01", TipoComprobante.FACTURA, "F001", "99"))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("motivo-invalido");
	}

	private static Nota nota(String serie, TipoComprobante tipoRef, String serieRef) {
		return nota(serie, tipoRef, serieRef, "01");
	}

	private static Nota nota(String serie, TipoComprobante tipoRef, String serieRef, String motivo) {
		LineaCalculada linea = new LineaCalculada(BigDecimal.ONE, TipoAfectacionIgv.GRAVADO_ONEROSO,
				new BigDecimal("118"), new BigDecimal("100"), new BigDecimal("18"), BigDecimal.ZERO,
				new BigDecimal("100"), new BigDecimal("18"), BigDecimal.ZERO, BigDecimal.ZERO);
		TotalesComprobante totales = new TotalesComprobante(new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("18"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100"),
				new BigDecimal("118"));
		return new Nota(null, TipoComprobante.NOTA_CREDITO, serie, 1, LocalDate.now(), LocalTime.NOON, Moneda.PEN,
				BigDecimal.ONE, 1L, DocumentoIdentidad.ruc("20601487871"), "Cliente", null,
				new ReferenciaComprobante(tipoRef, serieRef, 1), motivo, "Anulación",
				List.of(new LineaVenta(1L, "P1", "Prod", "NIU", linea)), totales, null, null, false);
	}

}
