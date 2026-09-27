package pe.facturacion.clientes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.clientes.application.port.out.PadronDocumentosPort;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

@ExtendWith(MockitoExtension.class)
class ConsultarPadronServiceTest {

	@Mock
	private PadronDocumentosPort padron;

	@Test
	void consultaDniYRuc() {
		DocumentoIdentidad dni = DocumentoIdentidad.dni("47204426");
		DatosPadron datos = new DatosPadron(dni, "PEREZ ANA", null, null, null, null, null, null, null);
		when(padron.consultar(dni)).thenReturn(Optional.of(datos));

		assertThat(new ConsultarPadronService(padron).consultar(dni)).contains(datos);
	}

	@Test
	void rechazaOtrosTiposDeDocumento() {
		DocumentoIdentidad carne = new DocumentoIdentidad(TipoDocumentoIdentidad.CARNET_EXTRANJERIA, "001234567");

		assertThatThrownBy(() -> new ConsultarPadronService(padron).consultar(carne))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("consulta-no-soportada");
		verify(padron, never()).consultar(any());
	}

}
