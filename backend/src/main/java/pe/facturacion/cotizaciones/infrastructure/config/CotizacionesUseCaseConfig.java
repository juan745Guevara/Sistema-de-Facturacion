package pe.facturacion.cotizaciones.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.cotizaciones.application.port.in.GestionarCotizacionesUseCase;
import pe.facturacion.cotizaciones.application.port.out.CotizacionRepositoryPort;
import pe.facturacion.cotizaciones.application.usecase.GestionarCotizacionesService;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.shared.application.port.out.Transacciones;

@Configuration(proxyBeanMethods = false)
class CotizacionesUseCaseConfig {

	@Bean
	GestionarCotizacionesUseCase gestionarCotizacionesUseCase(GestionarEmpresaUseCase empresas,
			GestionarSeriesUseCase series, GestionarClientesUseCase clientes, GestionarProductosUseCase productos,
			CotizacionRepositoryPort cotizaciones, Transacciones transacciones, Clock reloj) {
		return new GestionarCotizacionesService(empresas, series, clientes, productos, cotizaciones, transacciones,
				reloj);
	}

}
