package pe.facturacion.clientes.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

@Mapper(imports = DocumentoIdentidad.class)
interface ClientePersistenceMapper {

	@Mapping(target = "documento", expression = "java(new DocumentoIdentidad(entidad.getTipoDocumento(), entidad.getNumeroDocumento()))")
	@Mapping(target = "conId", ignore = true)
	Cliente aDominio(ClienteJpaEntity entidad);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tipoDocumento", source = "documento.tipo")
	@Mapping(target = "numeroDocumento", source = "documento.numero")
	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Cliente cliente, @MappingTarget ClienteJpaEntity entidad);

}
