package pe.facturacion.proveedores.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

@Mapper(imports = DocumentoIdentidad.class)
interface ProveedorPersistenceMapper {

	@Mapping(target = "documento", expression = "java(new DocumentoIdentidad(entidad.getTipoDocumento(), entidad.getNumeroDocumento()))")
	@Mapping(target = "conId", ignore = true)
	Proveedor aDominio(ProveedorJpaEntity entidad);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tipoDocumento", source = "documento.tipo")
	@Mapping(target = "numeroDocumento", source = "documento.numero")
	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Proveedor proveedor, @MappingTarget ProveedorJpaEntity entidad);

}
