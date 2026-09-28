package pe.facturacion.compras.infrastructure.web;

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

class ComprasIntegrationTest extends IntegracionTest {

	@Test
	void registraYAnulaUnaCompraAumentandoYDevolviendoStock() throws Exception {
		asegurarEmpresa();
		long categoria = id(postJson("/api/catalogo/categorias", Rol.ESPECIAL,
				"{\"nombre\": \"compras %s\"}".formatted(DatosDePrueba.unico())));
		String codigo = "CMP-" + DatosDePrueba.unico();
		long producto = id(postJson("/api/catalogo/productos", Rol.ADMINISTRADOR, """
				{"codigo": "%s", "descripcion": "Insumo", "categoriaId": %d, "unidadMedida": "NIU",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 10, "precioCompra": 5, "stock": 2}
				""".formatted(codigo, categoria)));
		long proveedor = id(postJson("/api/proveedores", Rol.ESPECIAL, """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Proveedor Sur"}
				""".formatted(DatosDePrueba.rucNuevo())));

		String compra = mvc.perform(post("/api/compras").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"tipo": "FACTURA", "serie": "F%s", "correlativo": "1", "proveedorId": %d,
								 "items": [{"productoId": %d, "cantidad": 3, "precioUnitario": 5}]}
								""".formatted(DatosDePrueba.unico(), proveedor, producto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.anulada").value(false))
				.andExpect(jsonPath("$.total").value(17.7))
				.andReturn().getResponse().getContentAsString();
		long compraId = ((Number) JsonPath.read(compra, "$.id")).longValue();

		mvc.perform(get("/api/catalogo/productos/{id}", producto).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock").value(5.0));

		mvc.perform(post("/api/compras/{id}/anular", compraId).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.anulada").value(true));

		mvc.perform(get("/api/catalogo/productos/{id}", producto).header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock").value(2.0));
	}

	private void asegurarEmpresa() throws Exception {
		mvc.perform(put("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"ruc": "20601487871", "razonSocial": "Mi Empresa S.A.C.",
								 "domicilioFiscal": {"direccion": "Av. Arequipa 123", "ubigeo": "150101",
								                     "departamento": "LIMA", "provincia": "LIMA", "distrito": "LIMA"},
								 "porcentajeIgv": 18, "bienesSelva": false, "serviciosSelva": false}
								"""))
				.andExpect(status().isOk());
	}

	private String postJson(String url, Rol rol, String cuerpo) throws Exception {
		return mvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, bearer(rol))
						.contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
	}

	private static long id(String json) {
		return ((Number) JsonPath.read(json, "$.id")).longValue();
	}

}
