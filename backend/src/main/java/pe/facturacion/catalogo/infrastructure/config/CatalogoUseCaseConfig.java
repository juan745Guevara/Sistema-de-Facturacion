package pe.facturacion.catalogo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarCategoriasUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarUnidadesMedidaUseCase;
import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.application.port.out.UnidadMedidaRepositoryPort;
import pe.facturacion.catalogo.application.usecase.AjustarStockService;
import pe.facturacion.catalogo.application.usecase.GestionarCategoriasService;
import pe.facturacion.catalogo.application.usecase.GestionarProductosService;
import pe.facturacion.catalogo.application.usecase.GestionarUnidadesMedidaService;

@Configuration(proxyBeanMethods = false)
class CatalogoUseCaseConfig {

	@Bean
	GestionarCategoriasUseCase gestionarCategoriasUseCase(CategoriaRepositoryPort categorias,
			ProductoRepositoryPort productos) {
		return new GestionarCategoriasService(categorias, productos);
	}

	@Bean
	GestionarUnidadesMedidaUseCase gestionarUnidadesMedidaUseCase(UnidadMedidaRepositoryPort unidades) {
		return new GestionarUnidadesMedidaService(unidades);
	}

	@Bean
	AjustarStockUseCase ajustarStockUseCase(ProductoRepositoryPort productos) {
		return new AjustarStockService(productos);
	}

	@Bean
	GestionarProductosUseCase gestionarProductosUseCase(ProductoRepositoryPort productos,
			CategoriaRepositoryPort categorias, UnidadMedidaRepositoryPort unidades) {
		return new GestionarProductosService(productos, categorias, unidades);
	}

}
