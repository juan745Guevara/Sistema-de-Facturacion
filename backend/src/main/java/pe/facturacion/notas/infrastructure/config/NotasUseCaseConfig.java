package pe.facturacion.notas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.notas.application.port.in.ConsultarNotasUseCase;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.application.usecase.ConsultarNotasService;
import pe.facturacion.notas.application.usecase.EmitirNotaService;
import pe.facturacion.notas.application.usecase.SincronizarEstadoNotaService;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;

@Configuration(proxyBeanMethods = false)
class NotasUseCaseConfig {

	@Bean
	EmitirNotaUseCase emitirNotaUseCase(GestionarEmpresaUseCase empresas, GestionarSeriesUseCase series,
			GestionarProductosUseCase productos, AjustarStockUseCase stock, ConsultarVentasUseCase ventas,
			NotaRepositoryPort notas, RegistrarDocumentoUseCase registrarSunat, EnviarDocumentoUseCase enviarSunat,
			Transacciones transacciones, Clock reloj) {
		return new EmitirNotaService(empresas, series, productos, stock, ventas, notas, registrarSunat, enviarSunat,
				transacciones, reloj);
	}

	@Bean
	ConsultarNotasUseCase consultarNotasUseCase(NotaRepositoryPort notas) {
		return new ConsultarNotasService(notas);
	}

	@Bean
	SincronizarEstadoNotaService sincronizarEstadoNotaService(NotaRepositoryPort notas, AjustarStockUseCase stock) {
		return new SincronizarEstadoNotaService(notas, stock);
	}

}
