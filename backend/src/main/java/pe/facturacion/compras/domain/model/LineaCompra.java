package pe.facturacion.compras.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import pe.facturacion.shared.domain.model.Textos;

public record LineaCompra(
		Long productoId,
		String codigo,
		String descripcion,
		String unidadMedida,
		BigDecimal cantidad,
		BigDecimal precioUnitario,
		BigDecimal valorVenta,
		BigDecimal impuesto) {

	public LineaCompra {
		codigo = Textos.obligatorio(codigo, "código", 30);
		descripcion = Textos.obligatorio(descripcion, "descripción", 500);
		unidadMedida = Textos.obligatorio(unidadMedida, "unidad de medida", 5);
		Objects.requireNonNull(cantidad, "cantidad");
		Objects.requireNonNull(precioUnitario, "precioUnitario");
		Objects.requireNonNull(valorVenta, "valorVenta");
		Objects.requireNonNull(impuesto, "impuesto");
	}

}
