package pe.facturacion.seguridad.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

import pe.facturacion.IntegracionTest;

class AuthIntegrationTest extends IntegracionTest {

	@Test
	void elAdministradorInicialIniciaSesionYConsultaSuPerfil() throws Exception {
		String respuesta = mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "ADMIN", "password": "clave-admin-de-test"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.token").value(notNullValue()))
				.andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
				.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(respuesta, "$.token");

		mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.nombre").value("Admin Test"))
				.andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
	}

	@Test
	void claveIncorrectaDevuelve401ConProblemDetail() throws Exception {
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "admin", "password": "otra-clave"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("credenciales-invalidas"));
	}

	@Test
	void cuerpoInvalidoDevuelve400ConErroresPorCampo() throws Exception {
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "", "password": ""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores[*].campo", hasItem("username")))
				.andExpect(jsonPath("$.errores[*].campo", hasItem("password")));
	}

	@Test
	void rutaProtegidaSinTokenDevuelve401() throws Exception {
		mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenFalsificadoDevuelve401() throws Exception {
		mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void corsPermiteElFrontendDeDesarrollo() throws Exception {
		mvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "http://localhost:4200")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
	}

	@Test
	void corsRechazaOtrosOrigenes() throws Exception {
		mvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "http://malicioso.example")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isForbidden());
	}

}
