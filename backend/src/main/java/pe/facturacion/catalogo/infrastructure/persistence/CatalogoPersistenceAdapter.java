package pe.facturacion.catalogo.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.application.port.out.UnidadMedidaRepositoryPort;
import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.catalogo.domain.model.UnidadMedida;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

final class CatalogoPersistenceAdapter {

	private CatalogoPersistenceAdapter() {
	}

	@Component
	@RequiredArgsConstructor
	@Transactional(readOnly = true)
	static class Categorias implements CategoriaRepositoryPort {

		private final CategoriaJpaRepository repositorio;
		private final CatalogoPersistenceMapper mapper;

		@Override
		public List<Categoria> listar() {
			return repositorio.findAll(Sort.by("nombre")).stream().map(mapper::aDominio).toList();
		}

		@Override
		public Optional<Categoria> buscarPorId(Long id) {
			return repositorio.findById(id).map(mapper::aDominio);
		}

		@Override
		public boolean existeNombre(String nombre, Long excluirId) {
			return excluirId == null ? repositorio.existsByNombre(nombre)
					: repositorio.existsByNombreAndIdNot(nombre, excluirId);
		}

		@Override
		@Transactional
		public Categoria guardar(Categoria categoria) {
			CategoriaJpaEntity entidad = categoria.id() == null ? new CategoriaJpaEntity()
					: repositorio.findById(categoria.id())
							.orElseThrow(() -> new RecursoNoEncontradoException("Categoría", categoria.id()));
			mapper.copiar(categoria, entidad);
			return mapper.aDominio(repositorio.saveAndFlush(entidad));
		}

		@Override
		@Transactional
		public void eliminar(Long id) {
			repositorio.deleteById(id);
		}

	}

	@Component
	@RequiredArgsConstructor
	@Transactional(readOnly = true)
	static class Unidades implements UnidadMedidaRepositoryPort {

		private static final Sort POR_DESCRIPCION = Sort.by("descripcion");

		private final UnidadMedidaJpaRepository repositorio;
		private final CatalogoPersistenceMapper mapper;

		@Override
		public List<UnidadMedida> listar(boolean soloActivas) {
			List<UnidadMedidaJpaEntity> encontradas = soloActivas ? repositorio.findByActivaTrue(POR_DESCRIPCION)
					: repositorio.findAll(POR_DESCRIPCION);
			return encontradas.stream().map(mapper::aDominio).toList();
		}

		@Override
		public Optional<UnidadMedida> buscarPorCodigo(String codigo) {
			return repositorio.findById(codigo).map(mapper::aDominio);
		}

		@Override
		@Transactional
		public UnidadMedida guardar(UnidadMedida unidad) {
			UnidadMedidaJpaEntity entidad = repositorio.findById(unidad.codigo())
					.orElseThrow(() -> new RecursoNoEncontradoException("Unidad de medida", unidad.codigo()));
			mapper.copiar(unidad, entidad);
			return mapper.aDominio(repositorio.saveAndFlush(entidad));
		}

	}

	@Component
	@RequiredArgsConstructor
	@Transactional(readOnly = true)
	static class Productos implements ProductoRepositoryPort {

		private final ProductoJpaRepository repositorio;
		private final CatalogoPersistenceMapper mapper;

		@Override
		public Pagina<Producto> buscar(ConsultaPaginada consulta, Long categoriaId) {
			return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta), categoriaId,
					Paginas.solicitud(consulta, Sort.by("descripcion"))), mapper::aDominio);
		}

		@Override
		public Optional<Producto> buscarPorId(Long id) {
			return repositorio.findById(id).map(mapper::aDominio);
		}

		@Override
		public boolean existeCodigo(String codigo, Long excluirId) {
			return excluirId == null ? repositorio.existsByCodigo(codigo)
					: repositorio.existsByCodigoAndIdNot(codigo, excluirId);
		}

		@Override
		public boolean existeConCategoria(Long categoriaId) {
			return repositorio.existsByCategoriaId(categoriaId);
		}

		@Override
		@Transactional
		public Producto guardar(Producto producto) {
			ProductoJpaEntity entidad = producto.id() == null ? new ProductoJpaEntity()
					: repositorio.findById(producto.id())
							.orElseThrow(() -> new RecursoNoEncontradoException("Producto", producto.id()));
			mapper.copiar(producto, entidad);
			return mapper.aDominio(repositorio.saveAndFlush(entidad));
		}

		@Override
		@Transactional
		public void eliminar(Long id) {
			repositorio.deleteById(id);
		}

	}

}
