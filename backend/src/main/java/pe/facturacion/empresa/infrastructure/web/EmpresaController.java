package pe.facturacion.empresa.infrastructure.web;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;

@Tag(name = "Empresa")
@RestController
@RequestMapping("/api/empresa")
@RequiredArgsConstructor
class EmpresaController {

	private final GestionarEmpresaUseCase empresa;
	private final EmpresaWebMapper mapper;

	@Operation(summary = "Datos del emisor")
	@GetMapping
	EmpresaDto obtener() {
		return mapper.aDto(empresa.obtener());
	}

	@Operation(summary = "Registra o actualiza los datos del emisor")
	@PutMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	EmpresaDto guardar(@Valid @RequestBody EmpresaDto datos) {
		return mapper.aDto(empresa.guardar(mapper.aDominio(datos)));
	}

}
