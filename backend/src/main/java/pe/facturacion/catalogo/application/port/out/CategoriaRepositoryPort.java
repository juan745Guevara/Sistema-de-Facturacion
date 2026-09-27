package pe.facturacion.catalogo.application.port.out;

import java.util.List;
import java.util.Optional;

import pe.facturacion.catalogo.domain.model.Categoria;

public interface CategoriaRepositoryPort {

	List<Categoria> listar();

	Optional<Categoria> buscarPorId(Long id);

	/** {@code excluirId} permite renombrar una categoría sin chocar consigo misma. */
	boolean existeNombre(String nombre, Long excluirId);

	Categoria guardar(Categoria categoria);

	void eliminar(Long id);

}
