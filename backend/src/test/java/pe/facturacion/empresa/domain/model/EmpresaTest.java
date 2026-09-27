package pe.facturacion.empresa.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pe.facturacion.shared.domain.exception.DominioException;

class EmpresaTest {

	static Direccion direccion() {
		return new Direccion("Av. Arequipa 123", "150101", "LIMA", "LIMA", "LIMA", null, null);
	}

	static Empresa empresa(String ruc, BigDecimal igv) {
		return new Empresa(ruc, "Mi Empresa S.A.C.", " ", direccion(), null, "ventas@empresa.pe", null, igv, false,
				false);
	}

	@Test
	void normalizaCamposOpcionalesYElIgv() {
		Empresa empresa = empresa("20601487871", new BigDecimal("18"));

		assertThat(empresa.nombreComercial()).isNull();
		assertThat(empresa.porcentajeIgv()).isEqualByComparingTo("18.00").hasScaleOf(2);
	}

	@Test
	void laDireccionUsaPeruYElDomicilioFiscalPorDefecto() {
		assertThat(direccion().codigoPais()).isEqualTo("PE");
		assertThat(direccion().codigoEstablecimiento()).isEqualTo("0000");
	}

	@Test
	void rechazaUnRucInvalido() {
		assertThatThrownBy(() -> empresa("20601487872", BigDecimal.TEN))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("documento-invalido");
	}

	@ParameterizedTest
	@ValueSource(strings = { "-1", "100", "18.005" })
	void rechazaUnIgvFueraDeRango(String igv) {
		assertThatThrownBy(() -> empresa("20601487871", new BigDecimal(igv)))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("igv-invalido");
	}

	@Test
	void elUbigeoTieneSeisDigitos() {
		assertThatThrownBy(() -> new Direccion("Av. 1", "1501", "LIMA", "LIMA", "LIMA", "PE", "0000"))
				.isInstanceOf(DominioException.class)
				.hasMessageContaining("ubigeo");
	}

}
