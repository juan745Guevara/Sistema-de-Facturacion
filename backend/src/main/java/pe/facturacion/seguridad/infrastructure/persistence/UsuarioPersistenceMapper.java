package pe.facturacion.seguridad.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pe.facturacion.seguridad.domain.model.Usuario;

@Mapper
interface UsuarioPersistenceMapper {

	@Mapping(target = "registrarIngreso", ignore = true)
	Usuario aDominio(UsuarioJpaEntity entidad);

	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Usuario usuario, @MappingTarget UsuarioJpaEntity entidad);

}
