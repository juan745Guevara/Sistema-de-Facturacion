package pe.facturacion.seguridad.application.usecase;

import java.util.List;
import java.util.Objects;

import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarUsuariosService implements GestionarUsuariosUseCase {

	private final UsuarioRepositoryPort usuarios;
	private final PasswordHasherPort hasher;

	public GestionarUsuariosService(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher) {
		this.usuarios = Objects.requireNonNull(usuarios);
		this.hasher = Objects.requireNonNull(hasher);
	}

	@Override
	public List<Usuario> listar() {
		return usuarios.listar();
	}

	@Override
	public Usuario crear(NuevoUsuario datos) {
		String username = Usuario.validarUsername(datos.username());
		Usuario.validarPassword(datos.password());
		if (usuarios.buscarPorUsername(username).isPresent()) {
			throw DominioException.conflicto("usuario-duplicado", "Ya existe el usuario " + username);
		}
		return usuarios.guardar(Usuario.nuevo(datos.nombre(), username, hasher.hashear(datos.password()),
				datos.email(), Objects.requireNonNull(datos.rol(), "rol")));
	}

	@Override
	public Usuario actualizar(Long id, CambiosUsuario cambios, Long idSolicitante) {
		Usuario actual = existente(id);
		if (id.equals(idSolicitante) && (!cambios.activo() || cambios.rol() != Rol.ADMINISTRADOR)) {
			throw DominioException.reglaNegocio("autobloqueo",
					"No puede desactivar su propio usuario ni quitarse el rol de administrador");
		}
		return usuarios.guardar(actual.actualizarDatos(cambios.nombre(), cambios.email(),
				Objects.requireNonNull(cambios.rol(), "rol"), cambios.activo()));
	}

	@Override
	public void cambiarPassword(Long id, String password) {
		Usuario actual = existente(id);
		Usuario.validarPassword(password);
		usuarios.guardar(actual.conPasswordHash(hasher.hashear(password)));
	}

	private Usuario existente(Long id) {
		return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
	}

}
