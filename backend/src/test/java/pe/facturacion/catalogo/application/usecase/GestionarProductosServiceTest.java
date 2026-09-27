package pe.facturacion.catalogo.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.application.port.out.UnidadMedidaRepositoryPort;
import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.catalogo.domain.model.UnidadMedida;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

@ExtendWith(MockitoExtension.class)
class GestionarProductosServiceTest {

	@Mock
	private ProductoRepositoryPort productos;
	@Mock
	private CategoriaRepositoryPort categorias;
	@Mock
	private UnidadMedidaRepositoryPort unidades;

	private GestionarProductosService servicio;

	@BeforeEach
	void setUp() {
		servicio = new GestionarProductosService(productos, categorias, unidades);
	}

	private static Producto producto(Long id, String unidad) {
		return new Producto(id, "P-001", "Gaseosa", 1L, unidad, TipoAfectacionIgv.GRAVADO_ONEROSO,
				new BigDecimal("3.50"), null, BigDecimal.TEN);
	}

	@Test
	void creaUnProductoIgnorandoElIdRecibido() {
		when(productos.existeCodigo("P-001", null)).thenReturn(false);
		when(categorias.buscarPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "BEBIDAS")));
		when(unidades.buscarPorCodigo("NIU")).thenReturn(Optional.of(new UnidadMedida("NIU", "UNIDADES", true)));
		when(productos.guardar(any())).thenAnswer(i -> i.getArgument(0));

		assertThat(servicio.crear(producto(77L, "NIU")).id()).isNull();
	}

	@Test
	void noPermiteCodigosRepetidos() {
		when(productos.existeCodigo("P-001", null)).thenReturn(true);

		assertThatThrownBy(() -> servicio.crear(producto(null, "NIU")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("producto-duplicado");
		verify(productos, never()).guardar(any());
	}

	@Test
	void exigeUnaCategoriaExistente() {
		when(productos.existeCodigo("P-001", null)).thenReturn(false);
		when(categorias.buscarPorId(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.crear(producto(null, "NIU")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("categoria-inexistente");
	}

	@Test
	void unProductoNuevoNoPuedeUsarUnaUnidadDesactivada() {
		when(productos.existeCodigo("P-001", null)).thenReturn(false);
		when(categorias.buscarPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "BEBIDAS")));
		when(unidades.buscarPorCodigo("KGM")).thenReturn(Optional.of(new UnidadMedida("KGM", "KILOGRAMO", false)));

		assertThatThrownBy(() -> servicio.crear(producto(null, "KGM")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("unidad-inactiva");
	}

	@Test
	void unProductoExistenteConservaSuUnidadAunqueSeDesactive() {
		when(productos.buscarPorId(3L)).thenReturn(Optional.of(producto(3L, "KGM")));
		when(productos.existeCodigo("P-001", 3L)).thenReturn(false);
		when(categorias.buscarPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "BEBIDAS")));
		when(unidades.buscarPorCodigo("KGM")).thenReturn(Optional.of(new UnidadMedida("KGM", "KILOGRAMO", false)));
		when(productos.guardar(any())).thenAnswer(i -> i.getArgument(0));

		assertThat(servicio.actualizar(3L, producto(null, "KGM")).id()).isEqualTo(3L);
	}

}
