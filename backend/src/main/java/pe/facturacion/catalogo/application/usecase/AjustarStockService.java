package pe.facturacion.catalogo.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.out.ProductoRepositoryPort;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class AjustarStockService implements AjustarStockUseCase {

	private final ProductoRepositoryPort productos;

	public AjustarStockService(ProductoRepositoryPort productos) {
		this.productos = productos;
	}

	@Override
	public void ajustar(Long productoId, BigDecimal variacion) {
		Objects.requireNonNull(variacion, "variacion");
		if (variacion.signum() != 0 && !productos.ajustarStock(productoId, variacion)) {
			throw new RecursoNoEncontradoException("Producto", productoId);
		}
	}

}
