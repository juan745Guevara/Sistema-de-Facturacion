package pe.facturacion.seguridad.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public record Usuario(
		Long id,
		String nombre,
		String username,
		String passwordHash,
		String email,
		Rol rol,
		boolean activo,
		Instant ultimoLogin) {

	public Usuario {
		Objects.requireNonNull(nombre, "nombre");
		Objects.requireNonNull(username, "username");
		Objects.requireNonNull(passwordHash, "passwordHash");
		Objects.requireNonNull(rol, "rol");
		username = normalizarUsername(username);
	}

	public static Usuario nuevo(String nombre, String username, String passwordHash, String email, Rol rol) {
		return new Usuario(null, nombre, username, passwordHash, email, rol, true, null);
	}

	public static String normalizarUsername(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	public Usuario registrarIngreso(Instant momento) {
		return new Usuario(id, nombre, username, passwordHash, email, rol, activo, momento);
	}

}
