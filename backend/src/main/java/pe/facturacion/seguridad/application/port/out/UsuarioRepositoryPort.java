package pe.facturacion.seguridad.application.port.out;

import java.util.Optional;

import pe.facturacion.seguridad.domain.model.Usuario;

public interface UsuarioRepositoryPort {

	Optional<Usuario> buscarPorUsername(String username);

	boolean existeAlguno();

	Usuario guardar(Usuario usuario);

}
