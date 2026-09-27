package pe.facturacion.clientes.infrastructure.web;

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
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.ClienteRequest;
import pe.facturacion.clientes.infrastructure.web.ClientesDtos.ClienteResponse;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
class ClienteController {

	private final GestionarClientesUseCase clientes;
	private final ClientesWebMapper mapper;

	@Operation(summary = "Busca clientes por nombre o número de documento")
	@GetMapping
	Pagina<ClienteResponse> buscar(@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return clientes.buscar(new ConsultaPaginada(q, pagina, tamanio)).map(mapper::aResponse);
	}

	@Operation(summary = "Obtiene un cliente")
	@GetMapping("/{id}")
	ClienteResponse obtener(@PathVariable Long id) {
		return mapper.aResponse(clientes.obtener(id));
	}

	@Operation(summary = "Registra un cliente")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
		return mapper.aResponse(clientes.crear(mapper.aDominio(request)));
	}

	@Operation(summary = "Actualiza un cliente")
	@PutMapping("/{id}")
	ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
		return mapper.aResponse(clientes.actualizar(id, mapper.aDominio(request)));
	}

	@Operation(summary = "Elimina un cliente")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	void eliminar(@PathVariable Long id) {
		clientes.eliminar(id);
	}

}
