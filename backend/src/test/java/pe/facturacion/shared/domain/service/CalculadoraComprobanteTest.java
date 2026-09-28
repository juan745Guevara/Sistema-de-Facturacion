package pe.facturacion.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ItemCalculo;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

class CalculadoraComprobanteTest {

	private static final BigDecimal IGV = new BigDecimal("18");
	private static final BigDecimal BOLSA = new BigDecimal("0.50");

	@Test
	void separaElIgvDeUnaLineaGravada() {
		ResultadoCalculo r = CalculadoraComprobante.calcular(
				List.of(new ItemCalculo(new BigDecimal("2"), new BigDecimal("118"), TipoAfectacionIgv.GRAVADO_ONEROSO,
						null, false)),
				IGV, DescuentoGlobal.ninguno(), BOLSA);

		assertThat(r.lineas().getFirst().valorUnitario()).isEqualByComparingTo("100");
		assertThat(r.lineas().getFirst().valorVenta()).isEqualByComparingTo("200.00");
		assertThat(r.lineas().getFirst().impuesto()).isEqualByComparingTo("36.00");
		assertThat(r.totales().total()).isEqualByComparingTo("236.00");
	}

	@Test
	void elDescuentoGlobalSoloAfectaGravadasYRecalculaElIgv() {
		ResultadoCalculo r = CalculadoraComprobante.calcular(List.of(
				new ItemCalculo(BigDecimal.ONE, new BigDecimal("118"), TipoAfectacionIgv.GRAVADO_ONEROSO, null, false),
				new ItemCalculo(BigDecimal.ONE, new BigDecimal("50"), TipoAfectacionIgv.EXONERADO_ONEROSO, null, false)),
				IGV, new DescuentoGlobal(DescuentoGlobal.Tipo.MONTO, new BigDecimal("10")), BOLSA);

		assertThat(r.totales().gravadas()).isEqualByComparingTo("90.00");
		assertThat(r.totales().igv()).isEqualByComparingTo("16.20");
		assertThat(r.totales().exoneradas()).isEqualByComparingTo("50.00");
		assertThat(r.totales().total()).isEqualByComparingTo("156.20");
	}

	@Test
	void lasGratuitasNoEntranAlTotalYElIcbperSi() {
		ResultadoCalculo r = CalculadoraComprobante.calcular(List.of(
				new ItemCalculo(new BigDecimal("3"), new BigDecimal("2"), TipoAfectacionIgv.INAFECTO_ONEROSO, null, true),
				new ItemCalculo(BigDecimal.ONE, new BigDecimal("10"), TipoAfectacionIgv.GRAVADO_RETIRO, null, false)),
				IGV, DescuentoGlobal.ninguno(), BOLSA);

		assertThat(r.totales().gratuitas()).isEqualByComparingTo("8.47");
		assertThat(r.totales().icbper()).isEqualByComparingTo("1.50");
		assertThat(r.totales().total()).isEqualByComparingTo("7.50");
	}

	@Test
	void rechazaDescuentoGlobalSinGravadas() {
		assertThatThrownBy(() -> CalculadoraComprobante.calcular(
				List.of(new ItemCalculo(BigDecimal.ONE, new BigDecimal("10"), TipoAfectacionIgv.EXONERADO_ONEROSO, null,
						false)),
				IGV, new DescuentoGlobal(DescuentoGlobal.Tipo.PORCENTAJE, new BigDecimal("10")), BOLSA))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("descuento-sin-gravadas");
	}

}
