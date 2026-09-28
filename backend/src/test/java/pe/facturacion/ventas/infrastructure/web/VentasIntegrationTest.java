package pe.facturacion.ventas.infrastructure.web;

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

class VentasIntegrationTest extends IntegracionTest {

	@Test
	void emiteNotaDeVentaYFacturaRecalculandoImportes() throws Exception {
		asegurarEmpresa();
		long categoria = id(postJson("/api/catalogo/categorias", Rol.ESPECIAL,
				"{\"nombre\": \"ventas %s\"}".formatted(DatosDePrueba.unico())));
		String codigo = "VT-" + DatosDePrueba.unico();
		long producto = id(postJson("/api/catalogo/productos", Rol.ADMINISTRADOR, """
				{"codigo": "%s", "descripcion": "Item", "categoriaId": %d, "unidadMedida": "NIU",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 118, "precioCompra": 80, "stock": 10}
				""".formatted(codigo, categoria)));
		long clienteRuc = id(postJson("/api/clientes", Rol.VENDEDOR, """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Cliente Factura"}
				""".formatted(DatosDePrueba.rucNuevo())));
		long clienteDni = id(postJson("/api/clientes", Rol.VENDEDOR, """
				{"tipoDocumento": "DNI", "numeroDocumento": "%s", "nombre": "Ana Perez"}
				""".formatted(DatosDePrueba.dniNuevo())));

		mvc.perform(post("/api/ventas/previsualizar").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content(items(producto, "118")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totales.total").value(118.0))
				.andExpect(jsonPath("$.totales.gravadas").value(100.0));

		mvc.perform(post("/api/ventas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content(emision("NOTA_VENTA", "N001", clienteDni, producto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.serie").value("N001"))
				.andExpect(jsonPath("$.estadoSunat").value("NO_APLICA"))
				.andExpect(jsonPath("$.totales.total").value(118.0));

		mvc.perform(post("/api/ventas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content(emision("FACTURA", "F001", clienteDni, producto)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.codigo").value("factura-sin-ruc"));

		mvc.perform(post("/api/ventas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content(emision("FACTURA", "F001", clienteRuc, producto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("FACTURA"))
				.andExpect(jsonPath("$.estadoSunat").value("PENDIENTE"));

		mvc.perform(get("/api/ventas").param("tipo", "FACTURA").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElementos").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
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

	private static String items(long productoId, String precio) {
		return """
				{"items": [{"productoId": %d, "cantidad": 1, "precioUnitario": %s}]}
				""".formatted(productoId, precio);
	}

	private static String emision(String tipo, String serie, long clienteId, long productoId) {
		return """
				{"tipo": "%s", "serie": "%s", "clienteId": %d, "moneda": "PEN", "formaPago": "CONTADO",
				 "items": [{"productoId": %d, "cantidad": 1}]}
				""".formatted(tipo, serie, clienteId, productoId);
	}

}
