package pe.facturacion.cotizaciones.infrastructure.web;

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

class CotizacionesIntegrationTest extends IntegracionTest {

	@Test
	void emiteUnaCotizacionSinMoverStock() throws Exception {
		asegurarEmpresa();
		long categoria = id(postJson("/api/catalogo/categorias", Rol.ESPECIAL,
				"{\"nombre\": \"cotiz %s\"}".formatted(DatosDePrueba.unico())));
		long producto = id(postJson("/api/catalogo/productos", Rol.ADMINISTRADOR, """
				{"codigo": "%s", "descripcion": "Servicio", "categoriaId": %d, "unidadMedida": "NIU",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 118, "precioCompra": 80, "stock": 4}
				""".formatted("CT-" + DatosDePrueba.unico(), categoria)));
		long cliente = id(postJson("/api/clientes", Rol.VENDEDOR, """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Cliente Cotizacion"}
				""".formatted(DatosDePrueba.rucNuevo())));

		mvc.perform(post("/api/cotizaciones").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"serie": "C001", "clienteId": %d,
								 "items": [{"productoId": %d, "cantidad": 1, "precioUnitario": 118}]}
								""".formatted(cliente, producto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.serie").value("C001"))
				.andExpect(jsonPath("$.total").value(118.0));
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
