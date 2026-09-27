package pe.facturacion.shared.domain.model.sunat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv.Tributo;

class CatalogosSunatTest {

	@ParameterizedTest(name = "afectación {0}")
	@CsvSource({
			"10, 1000, true,  false",
			"11, 9996, true,  true",
			"17, 1016, false, false",
			"20, 9997, false, false",
			"21, 9996, false, true",
			"30, 9998, false, false",
			"31, 9996, false, true",
			"40, 9995, false, false" })
	void afectacionIgvConSuTributo(String codigo, String tributo, boolean gravado, boolean gratuito) {
		TipoAfectacionIgv afectacion = TipoAfectacionIgv.desdeCodigo(codigo);

		assertThat(afectacion.tributo().codigo()).isEqualTo(tributo);
		assertThat(afectacion.gravado()).isEqualTo(gravado);
		assertThat(afectacion.gratuito()).isEqualTo(gratuito);
	}

	@Test
	void tributoIgvUsaCodigoInternacionalVat() {
		assertThat(Tributo.IGV.codigoInternacional()).isEqualTo("VAT");
		assertThat(Tributo.GRATUITO.codigoInternacional()).isEqualTo("FRE");
	}

	@Test
	void comprobantesInternosNoSonElectronicos() {
		assertThat(TipoComprobante.desdeCodigo("01").electronico()).isTrue();
		assertThat(TipoComprobante.NOTA_VENTA.electronico()).isFalse();
		assertThat(TipoComprobante.COTIZACION.electronico()).isFalse();
	}

	@Test
	void documentoIdentidadConLongitudFija() {
		assertThat(TipoDocumentoIdentidad.desdeCodigo("6").longitud()).isEqualTo(11);
		assertThat(TipoDocumentoIdentidad.DNI.longitud()).isEqualTo(8);
		assertThat(TipoDocumentoIdentidad.PASAPORTE.longitud()).isNull();
	}

	@Test
	void codigoDesconocidoFalla() {
		assertThatThrownBy(() -> TipoAfectacionIgv.desdeCodigo("99")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TipoComprobante.desdeCodigo("99")).isInstanceOf(IllegalArgumentException.class);
	}

}
