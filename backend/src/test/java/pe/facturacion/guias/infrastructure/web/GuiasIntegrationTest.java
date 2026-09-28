package pe.facturacion.guias.infrastructure.web;

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

class GuiasIntegrationTest extends IntegracionTest {

	@Test
	void emiteUnaGuiaPendienteYListaUbigeos() throws Exception {
		asegurarEmpresa();
		long categoria = id(postJson("/api/catalogo/categorias", Rol.ESPECIAL,
				"{\"nombre\": \"guias %s\"}".formatted(DatosDePrueba.unico())));
		long producto = id(postJson("/api/catalogo/productos", Rol.ADMINISTRADOR, """
				{"codigo": "%s", "descripcion": "Bulto", "categoriaId": %d, "unidadMedida": "NIU",
				 "tipoAfectacionIgv": "GRAVADO_ONEROSO", "precioVenta": 10, "precioCompra": 7, "stock": 8}
				""".formatted("GU-" + DatosDePrueba.unico(), categoria)));
		long cliente = id(postJson("/api/clientes", Rol.VENDEDOR, """
				{"tipoDocumento": "RUC", "numeroDocumento": "%s", "nombre": "Destinatario Guia"}
				""".formatted(DatosDePrueba.rucNuevo())));

		mvc.perform(get("/api/ubigeos").param("q", "lima").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].codigo").exists());

		mvc.perform(post("/api/guias").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"serie": "T001", "clienteId": %d, "motivoTraslado": "01", "modalidad": "02",
								 "fechaTraslado": "2026-09-27", "pesoTotal": 12.5, "bultos": 2,
								 "ubigeoPartida": "150101", "direccionPartida": "Av. Arequipa 100",
								 "ubigeoLlegada": "150122", "direccionLlegada": "Av. Larco 200",
								 "items": [{"productoId": %d, "cantidad": 1}]}
								""".formatted(cliente, producto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.serie").value("T001"))
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
