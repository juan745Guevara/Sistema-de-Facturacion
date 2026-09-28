package pe.facturacion.reportes.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase;
import pe.facturacion.reportes.application.usecase.ConsultarReportesService;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;

@Configuration(proxyBeanMethods = false)
class ReportesUseCaseConfig {

	@Bean
	ConsultarReportesUseCase consultarReportesUseCase(ConsultarVentasUseCase ventas, RegistrarCompraUseCase compras,
			ConsultarDocumentosUseCase documentos, Clock reloj) {
		return new ConsultarReportesService(ventas, compras, documentos, reloj);
	}

}
