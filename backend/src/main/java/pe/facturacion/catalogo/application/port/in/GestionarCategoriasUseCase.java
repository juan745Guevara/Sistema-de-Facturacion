package pe.facturacion.catalogo.application.port.in;

import java.util.List;

import pe.facturacion.catalogo.domain.model.Categoria;

public interface GestionarCategoriasUseCase {

	List<Categoria> listar();

	Categoria crear(String nombre);

	Categoria renombrar(Long id, String nombre);

	/** Solo se elimina si ningún producto la usa. */
	void eliminar(Long id);

}
