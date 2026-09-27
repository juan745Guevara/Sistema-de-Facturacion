package pe.facturacion.seguridad.application.port.in;

import java.util.List;

import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;

public interface GestionarUsuariosUseCase {

	record NuevoUsuario(String nombre, String username, String email, Rol rol, String password) {
	}

	record CambiosUsuario(String nombre, String email, Rol rol, boolean activo) {
	}

	List<Usuario> listar();

	Usuario crear(NuevoUsuario datos);

	/** {@code idSolicitante} es quien hace el cambio: no puede desactivarse ni quitarse el rol de administrador. */
	Usuario actualizar(Long id, CambiosUsuario cambios, Long idSolicitante);

	void cambiarPassword(Long id, String password);

}
