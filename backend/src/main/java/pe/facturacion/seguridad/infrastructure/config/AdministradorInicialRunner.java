package pe.facturacion.seguridad.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.port.in.CrearAdministradorInicialUseCase;
import pe.facturacion.seguridad.application.port.in.CrearAdministradorInicialUseCase.DatosAdministrador;

@Component
@RequiredArgsConstructor
class AdministradorInicialRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdministradorInicialRunner.class);

	private final CrearAdministradorInicialUseCase crearAdministrador;
	private final AdministradorInicialProperties propiedades;

	@Override
	public void run(ApplicationArguments args) {
		if (!propiedades.configurado()) {
			return;
		}
		boolean creado = crearAdministrador.crearSiNoHayUsuarios(
				new DatosAdministrador(propiedades.nombre(), propiedades.username(), propiedades.password()));
		if (creado) {
			log.info("Administrador inicial '{}' creado", propiedades.username());
		}
	}

}
