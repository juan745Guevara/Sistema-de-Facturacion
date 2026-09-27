package pe.facturacion.catalogo.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.catalogo.domain.model.UnidadMedida;

@Mapper
interface CatalogoPersistenceMapper {

	@Mapping(target = "renombrar", ignore = true)
	Categoria aDominio(CategoriaJpaEntity entidad);

	@Mapping(target = "conActivacion", ignore = true)
	UnidadMedida aDominio(UnidadMedidaJpaEntity entidad);

	@Mapping(target = "conId", ignore = true)
	Producto aDominio(ProductoJpaEntity entidad);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Categoria categoria, @MappingTarget CategoriaJpaEntity entidad);

	@Mapping(target = "codigo", ignore = true)
	@Mapping(target = "descripcion", ignore = true)
	void copiar(UnidadMedida unidad, @MappingTarget UnidadMedidaJpaEntity entidad);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Producto producto, @MappingTarget ProductoJpaEntity entidad);

}
