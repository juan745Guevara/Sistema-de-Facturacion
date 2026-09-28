package pe.facturacion.reportes.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import pe.facturacion.IntegracionTest;
import pe.facturacion.seguridad.domain.model.Rol;

class ReportesIntegrationTest extends IntegracionTest {

	@Test
	void exponeDashboardYRechazaCorreoSinSmtp() throws Exception {
		mvc.perform(get("/api/reportes/dashboard").header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.comprobantesPendientes").exists())
				.andExpect(jsonPath("$.ventasHoy").exists());

		mvc.perform(get("/api/reportes/ventas").param("desde", "2026-01-01").param("hasta", "2026-12-31")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.filas").isArray());

		mvc.perform(post("/api/reportes/correo").param("destinatario", "a@b.pe").param("tipo", "ventas")
						.param("desde", "2026-01-01").param("hasta", "2026-12-31")
						.header(HttpHeaders.AUTHORIZATION, bearer(Rol.VENDEDOR)))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.codigo").value("correo-no-configurado"));
	}

}
