package pe.facturacion.catalogo.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface ProductoRepositoryPort {

	Pagina<Producto> buscar(ConsultaPaginada consulta, Long categoriaId);

	Optional<Producto> buscarPorId(Long id);

	boolean existeCodigo(String codigo, Long excluirId);

	boolean existeConCategoria(Long categoriaId);

	Producto guardar(Producto producto);

	void eliminar(Long id);

	/** Devuelve {@code false} si el producto no existe. */
	boolean ajustarStock(Long id, BigDecimal variacion);

}
