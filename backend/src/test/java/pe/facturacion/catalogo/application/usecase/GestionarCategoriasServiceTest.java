package pe.facturacion.catalogo.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.catalogo.application.port.out.CategoriaRepositoryPort;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.catalogo.domain.model.Categoria;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;

@ExtendWith(MockitoExtension.class)
class GestionarCategoriasServiceTest {

	@Mock
	private CategoriaRepositoryPort categorias;
	@Mock
	private ProductoRepositoryPort productos;

	private GestionarCategoriasService servicio;

	@BeforeEach
	void setUp() {
		servicio = new GestionarCategoriasService(categorias, productos);
	}

	@Test
	void creaLaCategoriaEnMayusculas() {
		when(categorias.existeNombre("BEBIDAS", null)).thenReturn(false);
		when(categorias.guardar(any())).thenAnswer(i -> i.getArgument(0));

		assertThat(servicio.crear("bebidas").nombre()).isEqualTo("BEBIDAS");
	}

	@Test
	void noPermiteNombresRepetidos() {
		when(categorias.existeNombre("BEBIDAS", null)).thenReturn(true);

		assertThatThrownBy(() -> servicio.crear("Bebidas"))
				.isInstanceOf(DominioException.class)
				.extracting("tipo").isEqualTo(TipoError.CONFLICTO);
		verify(categorias, never()).guardar(any());
	}

	@Test
	void alRenombrarNoChocaConsigoMisma() {
		when(categorias.buscarPorId(5L)).thenReturn(Optional.of(new Categoria(5L, "BEBIDAS")));
		when(categorias.existeNombre("GASEOSAS", 5L)).thenReturn(false);
		when(categorias.guardar(any())).thenAnswer(i -> i.getArgument(0));

		assertThat(servicio.renombrar(5L, "gaseosas")).isEqualTo(new Categoria(5L, "GASEOSAS"));
	}

	@Test
	void noEliminaUnaCategoriaConProductos() {
		when(categorias.buscarPorId(5L)).thenReturn(Optional.of(new Categoria(5L, "BEBIDAS")));
		when(productos.existeConCategoria(5L)).thenReturn(true);

		assertThatThrownBy(() -> servicio.eliminar(5L))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("categoria-con-productos");
		verify(categorias, never()).eliminar(any());
	}

	@Test
	void eliminarUnaCategoriaInexistenteDevuelveNoEncontrado() {
		when(categorias.buscarPorId(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.eliminar(9L))
				.isInstanceOf(DominioException.class)
				.extracting("tipo").isEqualTo(TipoError.NO_ENCONTRADO);
	}

}
