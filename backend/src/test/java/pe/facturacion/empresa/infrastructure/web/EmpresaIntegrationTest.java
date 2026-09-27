package pe.facturacion.empresa.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import pe.facturacion.IntegracionTest;
import pe.facturacion.seguridad.domain.model.Rol;

class EmpresaIntegrationTest extends IntegracionTest {

	private static String empresa(String ruc) {
		return """
				{"ruc": "%s", "razonSocial": "Mi Empresa S.A.C.", "nombreComercial": "Mi Tienda",
				 "domicilioFiscal": {"direccion": "Av. Arequipa 123", "ubigeo": "150101", "departamento": "LIMA",
				                     "provincia": "LIMA", "distrito": "LIMA"},
				 "correoVentas": "ventas@mitienda.pe", "porcentajeIgv": 18, "bienesSelva": false,
				 "serviciosSelva": false}
				""".formatted(ruc);
	}

	@Test
	void soloElAdministradorRegistraLaEmpresaYTodosLaConsultan() throws Exception {
		mvc.perform(get("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("empresa-no-configurada"));

		mvc.perform(put("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR))
						.contentType(MediaType.APPLICATION_JSON).content(empresa("20601487871")))
				.andExpect(status().isForbidden());

		mvc.perform(put("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON).content(empresa("20601487871")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.porcentajeIgv").value(18.0))
				.andExpect(jsonPath("$.domicilioFiscal.codigoEstablecimiento").value("0000"));

		mvc.perform(get("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ruc").value("20601487871"))
				.andExpect(jsonPath("$.nombreComercial").value("Mi Tienda"));
	}

	@Test
	void rechazaDatosInvalidos() throws Exception {
		mvc.perform(put("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON).content(empresa("20601487872")))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.codigo").value("documento-invalido"));

		mvc.perform(put("/api/empresa").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ADMINISTRADOR))
						.contentType(MediaType.APPLICATION_JSON).content(empresa("123")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores[*].campo", hasItem("ruc")));
	}

}
