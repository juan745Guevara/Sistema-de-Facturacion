package pe.facturacion.proveedores.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import pe.facturacion.DatosDePrueba;
import pe.facturacion.IntegracionTest;
import pe.facturacion.seguridad.domain.model.Rol;

class ProveedoresIntegrationTest extends IntegracionTest {

	@Test
	void registraYBuscaProveedores() throws Exception {
		String ruc = DatosDePrueba.rucNuevo();
		String proveedor = """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Distribuidora del Sur SAC"}
				""".formatted(ruc);

		mvc.perform(post("/api/proveedores").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(proveedor))
				.andExpect(status().isForbidden());

		mvc.perform(post("/api/proveedores").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON).content(proveedor))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/proveedores").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON).content(proveedor))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("proveedor-duplicado"));

		mvc.perform(get("/api/proveedores").param("q", "distribuidora del sur")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido[?(@.numeroDocumento == '%s')]".formatted(ruc)).exists());
	}

}
