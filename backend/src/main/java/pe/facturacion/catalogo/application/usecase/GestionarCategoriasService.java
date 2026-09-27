package pe.facturacion.catalogo.application.usecase;

import java.util.List;
import java.util.Objects;

import pe.facturacion.catalogo.application.port.in.GestionarCategoriasUseCase;
import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarCategoriasService implements GestionarCategoriasUseCase {

	private final CategoriaRepositoryPort categorias;
	private final ProductoRepositoryPort productos;

	public GestionarCategoriasService(CategoriaRepositoryPort categorias, ProductoRepositoryPort productos) {
		this.categorias = Objects.requireNonNull(categorias);
		this.productos = Objects.requireNonNull(productos);
	}

	@Override
	public List<Categoria> listar() {
		return categorias.listar();
	}

	@Override
	public Categoria crear(String nombre) {
		Categoria nueva = Categoria.nueva(nombre);
		exigirNombreLibre(nueva.nombre(), null);
		return categorias.guardar(nueva);
	}

	@Override
	public Categoria renombrar(Long id, String nombre) {
		Categoria renombrada = existente(id).renombrar(nombre);
		exigirNombreLibre(renombrada.nombre(), id);
		return categorias.guardar(renombrada);
	}

	@Override
	public void eliminar(Long id) {
		existente(id);
		if (productos.existeConCategoria(id)) {
			throw DominioException.conflicto("categoria-con-productos",
					"No se puede eliminar la categoría porque tiene productos");
		}
		categorias.eliminar(id);
	}

	private Categoria existente(Long id) {
		return categorias.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
	}

	private void exigirNombreLibre(String nombre, Long excluirId) {
		if (categorias.existeNombre(nombre, excluirId)) {
			throw DominioException.conflicto("categoria-duplicada", "Ya existe la categoría " + nombre);
		}
	}

}
