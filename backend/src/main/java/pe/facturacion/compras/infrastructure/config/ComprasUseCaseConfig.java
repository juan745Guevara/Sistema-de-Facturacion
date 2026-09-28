package pe.facturacion.compras.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase;
import pe.facturacion.compras.application.port.out.CompraRepositoryPort;
import pe.facturacion.compras.application.usecase.RegistrarCompraService;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.proveedores.application.port.in.GestionarProveedoresUseCase;

@Configuration(proxyBeanMethods = false)
class ComprasUseCaseConfig {

	@Bean
	RegistrarCompraUseCase registrarCompraUseCase(GestionarProveedoresUseCase proveedores,
			GestionarProductosUseCase productos, GestionarEmpresaUseCase empresas, AjustarStockUseCase stock,
			CompraRepositoryPort compras) {
		return new RegistrarCompraService(proveedores, productos, empresas, stock, compras);
	}

}
