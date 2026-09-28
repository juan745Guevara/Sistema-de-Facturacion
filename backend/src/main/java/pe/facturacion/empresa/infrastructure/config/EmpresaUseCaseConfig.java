package pe.facturacion.empresa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.application.port.out.EmpresaRepositoryPort;
import pe.facturacion.empresa.application.port.out.SerieRepositoryPort;
import pe.facturacion.empresa.application.usecase.GestionarEmpresaService;
import pe.facturacion.empresa.application.usecase.GestionarSeriesService;

@Configuration(proxyBeanMethods = false)
class EmpresaUseCaseConfig {

	@Bean
	GestionarEmpresaUseCase gestionarEmpresaUseCase(EmpresaRepositoryPort empresas) {
		return new GestionarEmpresaService(empresas);
	}

	@Bean
	GestionarSeriesUseCase gestionarSeriesUseCase(SerieRepositoryPort series) {
		return new GestionarSeriesService(series);
	}

}
