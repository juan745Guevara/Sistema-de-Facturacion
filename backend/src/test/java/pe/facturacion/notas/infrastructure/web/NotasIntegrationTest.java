package pe.facturacion.notas.infrastructure.web;

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

class NotasIntegrationTest extends IntegracionTest {

	@Test
	void emiteNotaDeCreditoSobreUnaFactura() throws Exception {
		asegurarEmpresa();
		long categoria = id(postJson("/api/catalogo/categorias", Rol.ESPECIAL,
				"{\"nombre\": \"notas %s\"}".formatted(DatosDePrueba.unico())));
		String codigo = "NC-" + DatosDePrueba.unico();
		long producto = id(postJson("/api/catalogo/productos", Rol.ADMINISTRADOR, """
				{"codigo": "%s", "descripcion": "Item", "categoriaId": %d, "unidadMedida": "NIU",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 118, "precioCompra": 80, "stock": 10}
				""".formatted(codigo, categoria)));
		long clienteRuc = id(postJson("/api/clientes", Rol.VENDEDOR, """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Cliente Factura"}
				""".formatted(DatosDePrueba.rucNuevo())));

		String factura = mvc.perform(post("/api/ventas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"tipo": "FACTURA", "serie": "F001", "clienteId": %d, "moneda": "PEN",
								 "formaPago": "CONTADO", "items": [{"productoId": %d, "cantidad": 1}]}
								""".formatted(clienteRuc, producto)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		int correlativo = JsonPath.read(factura, "$.correlativo");

		mvc.perform(post("/api/notas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"tipo": "NOTA_CREDITO", "serie": "FC01", "tipoReferencia": "FACTURA",
								 "serieReferencia": "F001", "correlativoReferencia": %d, "codigoMotivo": "01"}
								""".formatted(correlativo)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("NOTA_CREDITO"))
				.andExpect(jsonPath("$.serie").value("FC01"))
				.andExpect(jsonPath("$.totales.total").value(118.0))
				.andExpect(jsonPath("$.estadoSunat").value("PENDIENTE"));
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
