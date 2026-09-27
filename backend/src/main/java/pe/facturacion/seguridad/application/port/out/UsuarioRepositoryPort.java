package pe.facturacion.seguridad.application.port.out;

import java.util.List;
import java.util.Optional;

import pe.facturacion.seguridad.domain.model.Usuario;

public interface UsuarioRepositoryPort {

	Optional<Usuario> buscarPorUsername(String username);

	Optional<Usuario> buscarPorId(Long id);

	/** Ordenados por nombre. */
	List<Usuario> listar();

	boolean existeAlguno();

	Usuario guardar(Usuario usuario);

}
