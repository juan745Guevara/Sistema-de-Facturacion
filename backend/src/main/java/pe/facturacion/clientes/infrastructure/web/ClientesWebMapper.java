package pe.facturacion.clientes.infrastructure.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.ClienteRequest;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.ClienteResponse;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.DatosPadronResponse;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

@Mapper(imports = DocumentoIdentidad.class)
interface ClientesWebMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "conId", ignore = true)
	@Mapping(target = "documento", expression = "java(new DocumentoIdentidad(request.tipoDocumento(), request.numeroDocumento()))")
	Cliente aDominio(ClienteRequest request);

	@Mapping(target = "tipoDocumento", source = "documento.tipo")
	@Mapping(target = "numeroDocumento", source = "documento.numero")
	ClienteResponse aResponse(Cliente cliente);

	@Mapping(target = "tipoDocumento", source = "documento.tipo")
	@Mapping(target = "numeroDocumento", source = "documento.numero")
	DatosPadronResponse aResponse(DatosPadron datos);

}
