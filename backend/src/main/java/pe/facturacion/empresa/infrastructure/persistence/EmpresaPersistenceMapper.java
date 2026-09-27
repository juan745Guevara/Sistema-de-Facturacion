package pe.facturacion.empresa.infrastructure.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pe.facturacion.empresa.domain.model.Empresa;

@Mapper
interface EmpresaPersistenceMapper {

	Empresa aDominio(EmpresaJpaEntity entidad);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "creadoEn", ignore = true)
	@Mapping(target = "creadoPor", ignore = true)
	@Mapping(target = "actualizadoEn", ignore = true)
	@Mapping(target = "actualizadoPor", ignore = true)
	void copiar(Empresa empresa, @MappingTarget EmpresaJpaEntity entidad);

}
