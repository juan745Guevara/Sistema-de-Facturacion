package pe.facturacion.clientes.infrastructure.padron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

class ApiFacturacionPadronAdapterTest {

	private static final String URL = "https://padron.example";

	private MockRestServiceServer servidor;
	private RestClient.Builder builder;

	@BeforeEach
	void setUp() {
		builder = RestClient.builder().baseUrl(URL);
		servidor = MockRestServiceServer.bindTo(builder).build();
	}

	private ApiFacturacionPadronAdapter adaptador(String token) {
		return new ApiFacturacionPadronAdapter(builder.build(), token);
	}

	@Test
	void consultaUnRucYMapeaLaRespuesta() {
		ApiFacturacionPadronAdapter padron = adaptador("token-prueba");
		servidor.expect(requestTo(URL + "/ruc/20601487871"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("token-prueba")))
				.andRespond(withSuccess("""
						{"ruc": "20601487871", "razon_social": "EMPRESA DEMO S.A.C.", "estado": "ACTIVO",
						 "condicion": "HABIDO", "direccion": "AV. LIMA 123", "ubigeo": "150101",
						 "departamento": "LIMA", "provincia": "LIMA", "distrito": "LIMA", "token": "x"}
						""", MediaType.APPLICATION_JSON));

		DatosPadron datos = padron.consultar(DocumentoIdentidad.ruc("20601487871")).orElseThrow();

		assertThat(datos.nombre()).isEqualTo("EMPRESA DEMO S.A.C.");
		assertThat(datos.ubigeo()).isEqualTo("150101");
		assertThat(datos.condicion()).isEqualTo("HABIDO");
		servidor.verify();
	}

	@Test
	void consultaUnDni() {
		ApiFacturacionPadronAdapter padron = adaptador("token-prueba");
		servidor.expect(requestTo(URL + "/dni/47204426"))
				.andRespond(withSuccess("""
						{"dni": "47204426", "cliente": "PEREZ GOMEZ ANA", "nombres": "ANA", "apellidos": "PEREZ GOMEZ"}
						""", MediaType.APPLICATION_JSON));

		assertThat(padron.consultar(DocumentoIdentidad.dni("47204426")))
				.get().extracting(DatosPadron::nombre).isEqualTo("PEREZ GOMEZ ANA");
	}

	@Test
	void devuelveVacioSiElDocumentoNoExiste() {
		ApiFacturacionPadronAdapter padron = adaptador("token-prueba");
		servidor.expect(requestTo(URL + "/dni/47204426")).andRespond(withResourceNotFound());

		assertThat(padron.consultar(DocumentoIdentidad.dni("47204426"))).isEmpty();
	}

	@Test
	void devuelveVacioSiLaRespuestaNoTraeDatos() {
		ApiFacturacionPadronAdapter padron = adaptador("token-prueba");
		servidor.expect(requestTo(URL + "/ruc/20601487871"))
				.andRespond(withSuccess("{\"success\": false}", MediaType.APPLICATION_JSON));

		assertThat(padron.consultar(DocumentoIdentidad.ruc("20601487871"))).isEmpty();
	}

	@Test
	void unErrorDelServicioSeInformaComoNoDisponible() {
		ApiFacturacionPadronAdapter padron = adaptador("token-prueba");
		servidor.expect(requestTo(URL + "/ruc/20601487871")).andRespond(withServerError());

		assertThatThrownBy(() -> padron.consultar(DocumentoIdentidad.ruc("20601487871")))
				.isInstanceOf(DominioException.class)
				.extracting("tipo", "codigo").containsExactly(TipoError.SERVICIO_NO_DISPONIBLE, "consulta-fallida");
	}

	@Test
	void sinTokenNoLlamaAlServicio() {
		ApiFacturacionPadronAdapter padron = adaptador(" ");

		assertThatThrownBy(() -> padron.consultar(DocumentoIdentidad.ruc("20601487871")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("consulta-no-configurada");
		servidor.verify();
	}

}
