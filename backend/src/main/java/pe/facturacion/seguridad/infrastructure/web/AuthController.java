package pe.facturacion.seguridad.infrastructure.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.dto.SesionIniciada;
import pe.facturacion.seguridad.application.port.in.AutenticarUsuarioUseCase;
import pe.facturacion.seguridad.application.port.in.AutenticarUsuarioUseCase.Credenciales;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.infrastructure.security.ClaimsJwt;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

	private final AutenticarUsuarioUseCase autenticarUsuario;

	@Operation(summary = "Inicia sesión y devuelve un JWT")
	@PostMapping("/login")
	LoginResponse login(@Valid @RequestBody LoginRequest request) {
		SesionIniciada sesion = autenticarUsuario.autenticar(new Credenciales(request.username(), request.password()));
		return LoginResponse.desde(sesion);
	}

	@Operation(summary = "Devuelve el usuario del token actual")
	@GetMapping("/me")
	UsuarioResponse usuarioActual(@AuthenticationPrincipal Jwt jwt) {
		List<String> roles = jwt.getClaimAsStringList(ClaimsJwt.ROLES);
		Number id = jwt.getClaim(ClaimsJwt.USUARIO_ID);
		return new UsuarioResponse(
				id == null ? null : id.longValue(),
				jwt.getClaimAsString(ClaimsJwt.NOMBRE),
				jwt.getSubject(),
				roles == null || roles.isEmpty() ? null : Rol.valueOf(roles.getFirst()));
	}

}
