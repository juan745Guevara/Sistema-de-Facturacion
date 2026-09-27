package pe.facturacion.clientes.infrastructure.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.clientes.application.port.in.ConsultarPadronUseCase;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.DatosPadronResponse;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/api/documentos-identidad")
@RequiredArgsConstructor
class DocumentoIdentidadController {

	private final ConsultarPadronUseCase padron;
	private final ClientesWebMapper mapper;

	@Operation(summary = "Consulta un DNI o RUC en el padrón externo para autocompletar datos")
	@GetMapping("/{tipo}/{numero}")
	DatosPadronResponse consultar(@PathVariable TipoDocumentoIdentidad tipo, @PathVariable String numero) {
		DocumentoIdentidad documento = new DocumentoIdentidad(tipo, numero);
		return padron.consultar(documento)
				.map(mapper::aResponse)
				.orElseThrow(() -> new DominioException(TipoError.NO_ENCONTRADO, "documento-no-encontrado",
						"No se encontró el %s %s".formatted(tipo.descripcion(), documento.numero())));
	}

}
