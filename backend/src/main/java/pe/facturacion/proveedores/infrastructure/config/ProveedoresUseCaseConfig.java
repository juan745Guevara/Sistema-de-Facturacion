package pe.facturacion.proveedores.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.proveedores.application.port.in.GestionarProveedoresUseCase;
import pe.facturacion.proveedores.application.port.out.ProveedorRepositoryPort;
import pe.facturacion.proveedores.application.usecase.GestionarProveedoresService;

@Configuration(proxyBeanMethods = false)
class ProveedoresUseCaseConfig {

	@Bean
	GestionarProveedoresUseCase gestionarProveedoresUseCase(ProveedorRepositoryPort proveedores) {
		return new GestionarProveedoresService(proveedores);
	}

}
