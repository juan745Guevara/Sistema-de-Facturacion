package pe.facturacion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

class ModularidadTest {

	private final ApplicationModules modulos = ApplicationModules.of(FacturacionApplication.class);

	@Test
	void losModulosRespetanSusLimites() {
		modulos.verify();
	}

	@Test
	void existenTodosLosModulosDeNegocio() {
		assertThat(modulos.stream().map(ApplicationModule::getIdentifier).map(Object::toString))
				.contains("shared", "seguridad", "empresa", "catalogo", "clientes", "proveedores", "ventas",
						"notas", "guias", "compras", "cotizaciones", "sunat", "reportes");
	}

}
