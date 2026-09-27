package pe.facturacion.empresa.infrastructure.web;

import org.mapstruct.Mapper;

import pe.facturacion.empresa.domain.model.Empresa;

@Mapper
interface EmpresaWebMapper {

	Empresa aDominio(EmpresaDto dto);

	EmpresaDto aDto(Empresa empresa);

}
