package pe.facturacion.catalogo.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Textos;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

/**
 * Producto o servicio. {@code precioVenta} es el precio al público: incluye IGV cuando la afectación es gravada.
 * El valor unitario sin IGV se calcula al vender, con el porcentaje vigente de la empresa.
 */
public record Producto(
		Long id,
		String codigo,
		String descripcion,
		Long categoriaId,
		String unidadMedida,
		TipoAfectacionIgv tipoAfectacionIgv,
		BigDecimal precioVenta,
		BigDecimal precioCompra,
		BigDecimal stock) {

	public Producto {
		codigo = Textos.obligatorio(codigo, "código", 30).toUpperCase(Locale.ROOT);
		descripcion = Textos.obligatorio(descripcion, "descripción", 500);
		Objects.requireNonNull(categoriaId, "categoriaId");
		unidadMedida = Textos.obligatorio(unidadMedida, "unidad de medida", 5).toUpperCase(Locale.ROOT);
		Objects.requireNonNull(tipoAfectacionIgv, "tipoAfectacionIgv");
		precioVenta = monto(precioVenta, "precio de venta", 2);
		if (precioVenta.signum() == 0) {
			throw DominioException.reglaNegocio("precio-invalido", "El precio de venta debe ser mayor que cero");
		}
		precioCompra = monto(precioCompra == null ? BigDecimal.ZERO : precioCompra, "precio de compra", 2);
		stock = monto(stock == null ? BigDecimal.ZERO : stock, "stock", 3);
	}

	public Producto conId(Long nuevoId) {
		return new Producto(nuevoId, codigo, descripcion, categoriaId, unidadMedida, tipoAfectacionIgv, precioVenta,
				precioCompra, stock);
	}

	private static BigDecimal monto(BigDecimal valor, String campo, int decimales) {
		if (valor == null || valor.signum() < 0 || valor.stripTrailingZeros().scale() > decimales) {
			throw DominioException.reglaNegocio("monto-invalido",
					"El %s debe ser un número no negativo con hasta %d decimales".formatted(campo, decimales));
		}
		return valor.setScale(decimales, RoundingMode.UNNECESSARY);
	}

}
