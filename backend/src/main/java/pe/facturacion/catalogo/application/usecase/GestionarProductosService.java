package pe.facturacion.catalogo.application.usecase;

import java.util.Objects;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.application.port.out.UnidadMedidaRepositoryPort;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.catalogo.domain.model.UnidadMedida;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarProductosService implements GestionarProductosUseCase {

	private final ProductoRepositoryPort productos;
	private final CategoriaRepositoryPort categorias;
	private final UnidadMedidaRepositoryPort unidades;

	public GestionarProductosService(ProductoRepositoryPort productos, CategoriaRepositoryPort categorias,
			UnidadMedidaRepositoryPort unidades) {
		this.productos = Objects.requireNonNull(productos);
		this.categorias = Objects.requireNonNull(categorias);
		this.unidades = Objects.requireNonNull(unidades);
	}

	@Override
	public Pagina<Producto> buscar(ConsultaPaginada consulta, Long categoriaId) {
		return productos.buscar(consulta, categoriaId);
	}

	@Override
	public Producto obtener(Long id) {
		return productos.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
	}

	@Override
	public Producto crear(Producto producto) {
		Producto nuevo = producto.conId(null);
		validarReferencias(nuevo, null);
		return productos.guardar(nuevo);
	}

	@Override
	public Producto actualizar(Long id, Producto producto) {
		Producto actual = obtener(id);
		Producto cambiado = producto.conId(id);
		validarReferencias(cambiado, actual.unidadMedida());
		return productos.guardar(cambiado);
	}

	@Override
	public void eliminar(Long id) {
		obtener(id);
		productos.eliminar(id);
	}

	/** Un producto que ya usa una unidad desactivada puede conservarla; uno nuevo no puede elegirla. */
	private void validarReferencias(Producto producto, String unidadActual) {
		if (productos.existeCodigo(producto.codigo(), producto.id())) {
			throw DominioException.conflicto("producto-duplicado",
					"Ya existe un producto con el código " + producto.codigo());
		}
		if (categorias.buscarPorId(producto.categoriaId()).isEmpty()) {
			throw DominioException.reglaNegocio("categoria-inexistente",
					"La categoría %d no existe".formatted(producto.categoriaId()));
		}
		UnidadMedida unidad = unidades.buscarPorCodigo(producto.unidadMedida())
				.orElseThrow(() -> DominioException.reglaNegocio("unidad-inexistente",
						"La unidad de medida %s no existe".formatted(producto.unidadMedida())));
		if (!unidad.activa() && !unidad.codigo().equals(unidadActual)) {
			throw DominioException.reglaNegocio("unidad-inactiva",
					"La unidad de medida %s está desactivada".formatted(unidad.codigo()));
		}
	}

}
