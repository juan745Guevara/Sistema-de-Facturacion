package pe.facturacion.seguridad.infrastructure.web;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;

final class UsuariosDtos {

	private UsuariosDtos() {
	}

	record NuevoUsuarioRequest(
			@NotBlank @Size(max = 150) String nombre,
			@NotBlank @Size(max = 50) String username,
			@Email @Size(max = 120) String email,
			@NotNull Rol rol,
			@NotBlank @Size(min = Usuario.LONGITUD_MINIMA_PASSWORD, max = 100) String password) {
	}

	record ActualizarUsuarioRequest(
			@NotBlank @Size(max = 150) String nombre,
			@Email @Size(max = 120) String email,
			@NotNull Rol rol,
			@NotNull Boolean activo) {
	}

	record CambiarPasswordRequest(@NotBlank @Size(min = Usuario.LONGITUD_MINIMA_PASSWORD, max = 100) String password) {
	}

	record UsuarioDetalleResponse(
			Long id,
			String nombre,
			String username,
			String email,
			Rol rol,
			boolean activo,
			Instant ultimoLogin) {

		static UsuarioDetalleResponse desde(Usuario usuario) {
			return new UsuarioDetalleResponse(usuario.id(), usuario.nombre(), usuario.username(), usuario.email(),
					usuario.rol(), usuario.activo(), usuario.ultimoLogin());
		}
	}

}
