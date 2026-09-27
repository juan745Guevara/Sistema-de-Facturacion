package pe.facturacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import pe.facturacion.seguridad.application.port.out.TokenEmisorPort;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;

/**
 * Base de las pruebas de integración. El contenedor de PostgreSQL es un bean de Spring, así que toda la suite
 * comparte un contenedor y un contexto: cada prueba debe usar datos propios y no asumir una base vacía.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegracionTest {

	@Autowired
	protected MockMvc mvc;

	@Autowired
	private TokenEmisorPort tokens;

	/** Cabecera Authorization de un usuario ficticio con el rol dado; el token está firmado como uno real. */
	protected String bearer(Rol rol) {
		Usuario usuario = new Usuario(9000L + rol.ordinal(), "Prueba " + rol, "prueba-" + rol.name(), "sin-uso",
				null, rol, true, null);
		return "Bearer " + tokens.emitir(usuario).valor();
	}

}
