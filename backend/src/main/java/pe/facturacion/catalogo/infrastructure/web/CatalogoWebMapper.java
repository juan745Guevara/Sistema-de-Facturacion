package pe.facturacion.catalogo.infrastructure.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.catalogo.domain.model.UnidadMedida;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.CategoriaResponse;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.ProductoRequest;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.ProductoResponse;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.UnidadMedidaResponse;

@Mapper
interface CatalogoWebMapper {

	CategoriaResponse aResponse(Categoria categoria);

	UnidadMedidaResponse aResponse(UnidadMedida unidad);

	ProductoResponse aResponse(Producto producto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "conId", ignore = true)
	Producto aDominio(ProductoRequest request);

}
