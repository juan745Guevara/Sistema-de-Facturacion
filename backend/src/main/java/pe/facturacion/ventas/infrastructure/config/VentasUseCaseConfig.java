package pe.facturacion.ventas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.application.usecase.ConsultarVentasService;
import pe.facturacion.ventas.application.usecase.EmitirVentaService;
import pe.facturacion.ventas.application.usecase.SincronizarEstadoVentaService;

@Configuration(proxyBeanMethods = false)
class VentasUseCaseConfig {

	@Bean
	EmitirVentaUseCase emitirVentaUseCase(GestionarEmpresaUseCase empresas, GestionarSeriesUseCase series,
			GestionarClientesUseCase clientes, GestionarProductosUseCase productos, AjustarStockUseCase stock,
			VentaRepositoryPort ventas, RegistrarDocumentoUseCase registrarSunat, EnviarDocumentoUseCase enviarSunat,
			Transacciones transacciones, Clock reloj) {
		return new EmitirVentaService(empresas, series, clientes, productos, stock, ventas, registrarSunat, enviarSunat,
				transacciones, reloj);
	}

	@Bean
	ConsultarVentasUseCase consultarVentasUseCase(VentaRepositoryPort ventas) {
		return new ConsultarVentasService(ventas);
	}

	@Bean
	SincronizarEstadoVentaService sincronizarEstadoVentaService(VentaRepositoryPort ventas, AjustarStockUseCase stock) {
		return new SincronizarEstadoVentaService(ventas, stock);
	}

}
