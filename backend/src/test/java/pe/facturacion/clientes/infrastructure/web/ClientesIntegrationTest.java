package pe.facturacion.clientes.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

class ClientesIntegrationTest extends IntegracionTest {

	private static String cliente(String tipo, String numero, String nombre) {
		return """
				{"tipoDocumento": "%s", "numeroDocumento": "%s", "nombre": "%s", "email": "compras@cliente.pe"}
				""".formatted(tipo, numero, nombre);
	}

	@Test
	void unVendedorRegistraYEditaClientesPeroNoLosElimina() throws Exception {
		String ruc = DatosDePrueba.rucNuevo();
		String respuesta = mvc.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(cliente("RUC", ruc, "Comercial Andina SAC")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipoDocumento").value("RUC"))
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(respuesta, "$.id")).longValue();

		mvc.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(cliente("RUC", ruc, "Otro nombre")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("cliente-duplicado"));

		mvc.perform(put("/api/clientes/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(cliente("RUC", ruc, "Comercial Andina S.A.C.")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Comercial Andina S.A.C."));

		mvc.perform(get("/api/clientes").param("q", ruc).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElementos").value(1))
				.andExpect(jsonPath("$.contenido[0].id").value(id));

		mvc.perform(delete("/api/clientes/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/api/clientes/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL)))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/clientes/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL)))
				.andExpect(status().isNotFound());
	}

	@Test
	void validaElDocumentoSegunSuTipo() throws Exception {
		mvc.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(cliente("DNI", "1234567", "Ana Pérez")))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.codigo").value("documento-invalido"));

		mvc.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(cliente("NO_EXISTE", "1", "Ana")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void laConsultaDeDocumentosSinTokenNoEstaDisponible() throws Exception {
		mvc.perform(get("/api/documentos-identidad/{tipo}/{numero}", "DNI", "47204426")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.codigo").value("consulta-no-configurada"));
	}

}
