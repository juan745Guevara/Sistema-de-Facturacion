package pe.facturacion.seguridad.application.usecase;

import java.util.Objects;

import pe.facturacion.seguridad.application.port.in.CrearAdministradorInicialUseCase;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;

public class CrearAdministradorInicialService implements CrearAdministradorInicialUseCase {

	static final int LONGITUD_MINIMA_PASSWORD = 12;

	private final UsuarioRepositoryPort usuarios;
	private final PasswordHasherPort hasher;

	public CrearAdministradorInicialService(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher) {
		this.usuarios = Objects.requireNonNull(usuarios);
		this.hasher = Objects.requireNonNull(hasher);
	}

	@Override
	public boolean crearSiNoHayUsuarios(DatosAdministrador datos) {
		Objects.requireNonNull(datos, "datos");
		if (usuarios.existeAlguno()) {
			return false;
		}
		if (datos.password() == null || datos.password().length() < LONGITUD_MINIMA_PASSWORD) {
			throw new IllegalArgumentException(
					"La contraseña del administrador inicial debe tener al menos %d caracteres"
							.formatted(LONGITUD_MINIMA_PASSWORD));
		}
		usuarios.guardar(Usuario.nuevo(datos.nombre(), datos.username(), hasher.hashear(datos.password()), null,
				Rol.ADMINISTRADOR));
		return true;
	}

}
