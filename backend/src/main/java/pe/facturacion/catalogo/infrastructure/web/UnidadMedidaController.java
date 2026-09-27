package pe.facturacion.catalogo.infrastructure.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.catalogo.application.port.in.GestionarUnidadesMedidaUseCase;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.ActivacionRequest;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.UnidadMedidaResponse;

@Tag(name = "Catálogo")
@RestController
@RequestMapping("/api/catalogo/unidades")
@RequiredArgsConstructor
class UnidadMedidaController {

	private final GestionarUnidadesMedidaUseCase unidades;
	private final CatalogoWebMapper mapper;

	@Operation(summary = "Lista las unidades de medida SUNAT")
	@GetMapping
	List<UnidadMedidaResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
		return unidades.listar(soloActivas).stream().map(mapper::aResponse).toList();
	}

	@Operation(summary = "Activa o desactiva una unidad de medida")
	@PatchMapping("/{codigo}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	UnidadMedidaResponse cambiarActivacion(@PathVariable String codigo,
			@Valid @RequestBody ActivacionRequest request) {
		return mapper.aResponse(unidades.cambiarActivacion(codigo, request.activa()));
	}

}
