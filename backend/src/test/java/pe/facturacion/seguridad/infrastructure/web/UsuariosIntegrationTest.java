package pe.facturacion.seguridad.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

import pe.facturacion.DatosDePrueba;
import pe.facturacion.IntegracionTest;
import pe.facturacion.seguridad.domain.model.Rol;

class UsuariosIntegrationTest extends IntegracionTest {

	private String login(String username, String password) throws Exception {
		String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\": \"%s\", \"password\": \"%s\"}".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(respuesta, "$.token");
	}

	@Test
	void elAdministradorCreaUnVendedorQuePuedeIngresarYLuegoLoDesactiva() throws Exception {
		String username = "vendedor." + DatosDePrueba.unico().toLowerCase();
		String admin = login("admin", "clave-admin-de-test");

		String respuesta = mvc.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, admin)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre": "Luis Vendedor", "username": "%s", "rol": "VENDEDOR",
								 "password": "clave-del-vendedor"}
								""".formatted(username)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.activo").value(true))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(respuesta, "$.id")).longValue();

		String vendedor = login(username, "clave-del-vendedor");
		mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, vendedor))
				.andExpect(status().isForbidden());

		mvc.perform(put("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\": \"Luis Vendedor\", \"rol\": \"VENDEDOR\", \"activo\": false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));

		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\": \"%s\", \"password\": \"clave-del-vendedor\"}".formatted(username)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void elAdministradorNoPuedeDesactivarseASiMismo() throws Exception {
		String admin = login("admin", "clave-admin-de-test");
		String yo = mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, admin))
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(yo, "$.id")).longValue();

		mvc.perform(put("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\": \"Admin Test\", \"rol\": \"ADMINISTRADOR\", \"activo\": false}"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.codigo").value("autobloqueo"));
	}

	@Test
	void validaLaLongitudDeLaClave() throws Exception {
		mvc.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre": "Corta", "username": "corta", "rol": "VENDEDOR", "password": "123"}
								"""))
				.andExpect(status().isBadRequest());
	}

}
