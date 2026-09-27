package pe.facturacion.proveedores.infrastructure.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.proveedores.infrastructure.web.ProveedoresDtos.ProveedorRequest;
import pe.facturacion.proveedores.infrastructure.web.ProveedoresDtos.ProveedorResponse;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

@Mapper(imports = DocumentoIdentidad.class)
interface ProveedoresWebMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "conId", ignore = true)
	@Mapping(target = "documento", expression = "java(new DocumentoIdentidad(request.tipoDocumento(), request.numeroDocumento()))")
	Proveedor aDominio(ProveedorRequest request);

	@Mapping(target = "tipoDocumento", source = "documento.tipo")
	@Mapping(target = "numeroDocumento", source = "documento.numero")
	ProveedorResponse aResponse(Proveedor proveedor);

}
