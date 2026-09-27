package pe.facturacion.seguridad.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.seguridad.application.port.in.AutenticarUsuarioUseCase;
import pe.facturacion.seguridad.application.port.in.CrearAdministradorInicialUseCase;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.application.usecase.AutenticarUsuarioService;
import pe.facturacion.seguridad.application.usecase.CrearAdministradorInicialService;

@Configuration(proxyBeanMethods = false)
class SeguridadUseCaseConfig {

	@Bean
	AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher,
			TokenEmisorPort tokens, Clock reloj) {
		return new AutenticarUsuarioService(usuarios, hasher, tokens, reloj);
	}

	@Bean
	CrearAdministradorInicialUseCase crearAdministradorInicialUseCase(UsuarioRepositoryPort usuarios,
			PasswordHasherPort hasher) {
		return new CrearAdministradorInicialService(usuarios, hasher);
	}

}
