package pe.facturacion.guias.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.guias.application.port.in.GestionarGuiasUseCase;
import pe.facturacion.guias.application.port.out.GuiaRepositoryPort;
import pe.facturacion.guias.application.usecase.GestionarGuiasService;
import pe.facturacion.shared.application.port.out.Transacciones;

@Configuration(proxyBeanMethods = false)
class GuiasUseCaseConfig {

	@Bean
	GestionarGuiasUseCase gestionarGuiasUseCase(GestionarSeriesUseCase series, GestionarClientesUseCase clientes,
			GestionarProductosUseCase productos, GuiaRepositoryPort guias, Transacciones transacciones, Clock reloj) {
		return new GestionarGuiasService(series, clientes, productos, guias, transacciones, reloj);
	}

}
