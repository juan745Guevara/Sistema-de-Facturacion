package pe.facturacion.seguridad.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Textos;

public record Usuario(
		Long id,
		String nombre,
		String username,
		String passwordHash,
		String email,
		Rol rol,
		boolean activo,
		Instant ultimoLogin) {

	public static final int LONGITUD_MINIMA_PASSWORD = 12;

	public Usuario {
		nombre = Textos.obligatorio(nombre, "nombre", 150);
		Objects.requireNonNull(username, "username");
		Objects.requireNonNull(passwordHash, "passwordHash");
		email = Textos.opcional(email, "correo", 120);
		Objects.requireNonNull(rol, "rol");
		username = normalizarUsername(username);
	}

	public static Usuario nuevo(String nombre, String username, String passwordHash, String email, Rol rol) {
		return new Usuario(null, nombre, username, passwordHash, email, rol, true, null);
	}

	public static String normalizarUsername(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	/** Para usuarios creados desde la pantalla: letras, números, punto, guion y guion bajo. */
	public static String validarUsername(String username) {
		String normalizado = username == null ? "" : normalizarUsername(username);
		if (!normalizado.matches("[a-z0-9._-]{3,50}")) {
			throw DominioException.reglaNegocio("username-invalido",
					"El usuario debe tener de 3 a 50 caracteres: letras, números, punto, guion o guion bajo");
		}
		return normalizado;
	}

	public static void validarPassword(String password) {
		if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
			throw DominioException.reglaNegocio("password-corta",
					"La contraseña debe tener al menos %d caracteres".formatted(LONGITUD_MINIMA_PASSWORD));
		}
	}

	public Usuario registrarIngreso(Instant momento) {
		return new Usuario(id, nombre, username, passwordHash, email, rol, activo, momento);
	}

	public Usuario actualizarDatos(String nuevoNombre, String nuevoEmail, Rol nuevoRol, boolean estaActivo) {
		return new Usuario(id, nuevoNombre, username, passwordHash, nuevoEmail, nuevoRol, estaActivo, ultimoLogin);
	}

	public Usuario conPasswordHash(String nuevoHash) {
		return new Usuario(id, nombre, username, nuevoHash, email, rol, activo, ultimoLogin);
	}

}
