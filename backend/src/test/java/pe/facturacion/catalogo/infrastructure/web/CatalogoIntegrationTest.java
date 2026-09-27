package pe.facturacion.catalogo.infrastructure.web;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

class CatalogoIntegrationTest extends IntegracionTest {

	private long crearCategoria(String nombre) throws Exception {
		String respuesta = mvc.perform(post("/api/catalogo/categorias")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\": \"%s\"}".formatted(nombre)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
	}

	private static String producto(String codigo, long categoriaId, String unidad) {
		return """
				{"codigo": "%s", "descripcion": "Gaseosa %s 500 ml", "categoriaId": %d, "unidadMedida": "%s",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 3.50, "precioCompra": 2.45, "stock": 24}
				""".formatted(codigo, codigo, categoriaId, unidad);
	}

	@Test
	void registraYBuscaProductos() throws Exception {
		String codigo = "GAS-" + DatosDePrueba.unico();
		long categoria = crearCategoria("bebidas " + DatosDePrueba.unico());

		mvc.perform(post("/api/catalogo/productos").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON).content(producto(codigo, categoria, "NIU")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.precioVenta").value(3.5))
				.andExpect(jsonPath("$.stock").value(24.0));

		mvc.perform(get("/api/catalogo/productos").param("q", codigo.toLowerCase())
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElementos").value(1))
				.andExpect(jsonPath("$.contenido[0].codigo").value(codigo))
				.andExpect(jsonPath("$.contenido[0].tipoAfectacionIgv").value("GRAVADO_ONEROSO"));

		mvc.perform(post("/api/catalogo/productos").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON).content(producto(codigo, categoria, "NIU")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("producto-duplicado"));

		mvc.perform(delete("/api/catalogo/categorias/{id}", categoria)
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("categoria-con-productos"));
	}

	@Test
	void unVendedorNoModificaElCatalogo() throws Exception {
		mvc.perform(post("/api/catalogo/categorias").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"LIMPIEZA\"}"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/catalogo/productos").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(producto("X-1", 1, "NIU")))
				.andExpect(status().isForbidden());
	}

	@Test
	void lasUnidadesDesactivadasNoSeOfrecenNiSeAsignan() throws Exception {
		mvc.perform(get("/api/catalogo/unidades").param("soloActivas", "true")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].codigo", hasItem("NIU")))
				.andExpect(jsonPath("$[*].codigo", not(hasItem("YDK"))))
				.andExpect(jsonPath("$[*].activa", everyItem(org.hamcrest.Matchers.is(true))));

		long categoria = crearCategoria("ferreteria " + DatosDePrueba.unico());
		mvc.perform(post("/api/catalogo/productos").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content(producto("YD-" + DatosDePrueba.unico(), categoria, "YDK")))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.codigo").value("unidad-inactiva"));

		mvc.perform(patch("/api/catalogo/unidades/{codigo}", "YDK")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON).content("{\"activa\": true}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void renombraYEliminaUnaCategoriaVacia() throws Exception {
		String nombre = "temporal " + DatosDePrueba.unico();
		long categoria = crearCategoria(nombre);

		mvc.perform(post("/api/catalogo/categorias").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"%s\"}".formatted(nombre)))
				.andExpect(status().isConflict());

		mvc.perform(put("/api/catalogo/categorias/{id}", categoria)
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\": \"%s renombrada\"}".formatted(nombre)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value((nombre + " renombrada").toUpperCase()));

		mvc.perform(delete("/api/catalogo/categorias/{id}", categoria)
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.ESPECIAL)))
				.andExpect(status().isNoContent());
	}

}
