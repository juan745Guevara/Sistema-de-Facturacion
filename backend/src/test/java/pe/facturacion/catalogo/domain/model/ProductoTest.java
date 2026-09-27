package pe.facturacion.catalogo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

class ProductoTest {

	static Producto producto(String precioVenta, String precioCompra, String stock) {
		return new Producto(null, " p-001 ", "Gaseosa 500 ml", 1L, "niu", TipoAfectacionIgv.GRAVADO_ONEROSO,
				precioVenta == null ? null : new BigDecimal(precioVenta),
				precioCompra == null ? null : new BigDecimal(precioCompra),
				stock == null ? null : new BigDecimal(stock));
	}

	@Test
	void normalizaCodigoUnidadYMontos() {
		Producto producto = producto("3.5", null, null);

		assertThat(producto.codigo()).isEqualTo("P-001");
		assertThat(producto.unidadMedida()).isEqualTo("NIU");
		assertThat(producto.precioVenta()).isEqualTo(new BigDecimal("3.50"));
		assertThat(producto.precioCompra()).isEqualTo(new BigDecimal("0.00"));
		assertThat(producto.stock()).isEqualTo(new BigDecimal("0.000"));
	}

	@Test
	void elPrecioDeVentaDebeSerMayorQueCero() {
		assertThatThrownBy(() -> producto("0", "1", "1"))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("precio-invalido");
	}

	@Test
	void rechazaMontosNegativosODemasiadosDecimales() {
		assertThatThrownBy(() -> producto("-1", null, null)).isInstanceOf(DominioException.class);
		assertThatThrownBy(() -> producto("1.005", null, null)).isInstanceOf(DominioException.class);
		assertThatThrownBy(() -> producto("1", "-0.01", null)).isInstanceOf(DominioException.class);
		assertThatThrownBy(() -> producto("1", null, "1.0005")).isInstanceOf(DominioException.class);
	}

	@Test
	void laCategoriaSeGuardaEnMayusculas() {
		assertThat(Categoria.nueva(" bebidas ").nombre()).isEqualTo("BEBIDAS");
		assertThatThrownBy(() -> Categoria.nueva(" ")).isInstanceOf(DominioException.class);
	}

}
