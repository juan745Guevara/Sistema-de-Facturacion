package pe.facturacion.catalogo.application.port.in;

import java.math.BigDecimal;

public interface AjustarStockUseCase {

	/** Suma {@code variacion} al stock (negativa al vender). Se permite quedar en negativo, como en el sistema anterior. */
	void ajustar(Long productoId, BigDecimal variacion);

}
