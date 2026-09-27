package pe.facturacion.clientes.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.clientes.application.port.in.ConsultarPadronUseCase;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.application.port.out.ClienteRepositoryPort;
import pe.facturacion.clientes.application.port.out.PadronDocumentosPort;
import pe.facturacion.clientes.application.usecase.ConsultarPadronService;
import pe.facturacion.clientes.application.usecase.GestionarClientesService;

@Configuration(proxyBeanMethods = false)
class ClientesConfig {

	@Bean
	GestionarClientesUseCase gestionarClientesUseCase(ClienteRepositoryPort clientes) {
		return new GestionarClientesService(clientes);
	}

	@Bean
	ConsultarPadronUseCase consultarPadronUseCase(PadronDocumentosPort padron) {
		return new ConsultarPadronService(padron);
	}

}
