package pe.facturacion.ventas.domain.model;

import java.util.Objects;

import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.Textos;

/** Instantánea del producto al momento de vender. El catálogo puede cambiar después. */
public record LineaVenta(
		Long productoId,
		String codigo,
		String descripcion,
		String unidadMedida,
		LineaCalculada calculo) {

	public LineaVenta {
		codigo = Textos.obligatorio(codigo, "código", 30);
		descripcion = Textos.obligatorio(descripcion, "descripción", 500);
		unidadMedida = Textos.obligatorio(unidadMedida, "unidad de medida", 5);
		Objects.requireNonNull(calculo, "calculo");
	}

}
