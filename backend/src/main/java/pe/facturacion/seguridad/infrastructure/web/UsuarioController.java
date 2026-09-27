package pe.facturacion.seguridad.infrastructure.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase;
import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase.CambiosUsuario;
import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase.NuevoUsuario;
import pe.facturacion.seguridad.infrastructure.security.ClaimsJwt;
import pe.facturacion.seguridad.infrastructure.web.UsuariosDtos.ActualizarUsuarioRequest;
import pe.facturacion.seguridad.infrastructure.web.UsuariosDtos.CambiarPasswordRequest;
import pe.facturacion.seguridad.infrastructure.web.UsuariosDtos.NuevoUsuarioRequest;
import pe.facturacion.seguridad.infrastructure.web.UsuariosDtos.UsuarioDetalleResponse;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
class UsuarioController {

	private final GestionarUsuariosUseCase usuarios;

	@Operation(summary = "Lista los usuarios del sistema")
	@GetMapping
	List<UsuarioDetalleResponse> listar() {
		return usuarios.listar().stream().map(UsuarioDetalleResponse::desde).toList();
	}

	@Operation(summary = "Crea un usuario")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	UsuarioDetalleResponse crear(@Valid @RequestBody NuevoUsuarioRequest request) {
		return UsuarioDetalleResponse.desde(usuarios.crear(new NuevoUsuario(request.nombre(), request.username(),
				request.email(), request.rol(), request.password())));
	}

	@Operation(summary = "Actualiza nombre, correo, rol y estado de un usuario")
	@PutMapping("/{id}")
	UsuarioDetalleResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Number solicitante = jwt.getClaim(ClaimsJwt.USUARIO_ID);
		CambiosUsuario cambios = new CambiosUsuario(request.nombre(), request.email(), request.rol(),
				request.activo());
		return UsuarioDetalleResponse.desde(usuarios.actualizar(id, cambios,
				solicitante == null ? null : solicitante.longValue()));
	}

	@Operation(summary = "Asigna una nueva contraseña a un usuario")
	@PutMapping("/{id}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void cambiarPassword(@PathVariable Long id, @Valid @RequestBody CambiarPasswordRequest request) {
		usuarios.cambiarPassword(id, request.password());
	}

}
