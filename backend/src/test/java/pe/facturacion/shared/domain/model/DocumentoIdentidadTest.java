package pe.facturacion.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

class DocumentoIdentidadTest {

	@ParameterizedTest
	@ValueSource(strings = { "20601487871", "10472044261", "20100070970" })
	void aceptaRucConDigitoVerificadorCorrecto(String ruc) {
		assertThat(DocumentoIdentidad.ruc(ruc).numero()).isEqualTo(ruc);
	}

	@ParameterizedTest
	@ValueSource(strings = { "20601487872", "30601487871", "2060148787", "2060148787A", "" })
	void rechazaRucInvalido(String ruc) {
		assertThatThrownBy(() -> DocumentoIdentidad.ruc(ruc))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("documento-invalido");
	}

	@Test
	void elDniTieneOchoDigitos() {
		assertThat(DocumentoIdentidad.dni(" 47204426 ").numero()).isEqualTo("47204426");
		assertThatThrownBy(() -> DocumentoIdentidad.dni("4720442")).isInstanceOf(DominioException.class);
		assertThatThrownBy(() -> DocumentoIdentidad.dni("4720442X")).isInstanceOf(DominioException.class);
	}

	@Test
	void sinDocumentoUsaGuionSiNoHayNumero() {
		assertThat(new DocumentoIdentidad(TipoDocumentoIdentidad.SIN_DOCUMENTO, null).numero()).isEqualTo("-");
	}

	@Test
	void otrosDocumentosSeNormalizanAMayusculas() {
		DocumentoIdentidad pasaporte = new DocumentoIdentidad(TipoDocumentoIdentidad.PASAPORTE, " ab123456 ");

		assertThat(pasaporte.numero()).isEqualTo("AB123456");
		assertThat(pasaporte.esRuc()).isFalse();
		assertThatThrownBy(() -> new DocumentoIdentidad(TipoDocumentoIdentidad.PASAPORTE, "1234567890123456"))
				.isInstanceOf(DominioException.class);
	}

}
