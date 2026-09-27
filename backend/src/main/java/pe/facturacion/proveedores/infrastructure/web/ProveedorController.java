package pe.facturacion.proveedores.infrastructure.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.proveedores.application.port.in.GestionarProveedoresUseCase;
import pe.facturacion.proveedores.infrastructure.web.ProveedoresDtos.ProveedorRequest;
import pe.facturacion.proveedores.infrastructure.web.ProveedoresDtos.ProveedorResponse;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

@Tag(name = "Proveedores")
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
class ProveedorController {

	private final GestionarProveedoresUseCase proveedores;
	private final ProveedoresWebMapper mapper;

	@Operation(summary = "Busca proveedores por nombre o número de documento")
	@GetMapping
	Pagina<ProveedorResponse> buscar(@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return proveedores.buscar(new ConsultaPaginada(q, pagina, tamanio)).map(mapper::aResponse);
	}

	@Operation(summary = "Obtiene un proveedor")
	@GetMapping("/{id}")
	ProveedorResponse obtener(@PathVariable Long id) {
		return mapper.aResponse(proveedores.obtener(id));
	}

	@Operation(summary = "Registra un proveedor")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	ProveedorResponse crear(@Valid @RequestBody ProveedorRequest request) {
		return mapper.aResponse(proveedores.crear(mapper.aDominio(request)));
	}

	@Operation(summary = "Actualiza un proveedor")
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	ProveedorResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
		return mapper.aResponse(proveedores.actualizar(id, mapper.aDominio(request)));
	}

	@Operation(summary = "Elimina un proveedor")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	void eliminar(@PathVariable Long id) {
		proveedores.eliminar(id);
	}

}
