package pe.facturacion.guias.domain.model;

import java.math.BigDecimal;

import pe.facturacion.shared.domain.model.Textos;

public record LineaGuia(Long productoId, String codigo, String descripcion, String unidadMedida, BigDecimal cantidad) {

	public LineaGuia {
		codigo = Textos.obligatorio(codigo, "código", 30);
		descripcion = Textos.obligatorio(descripcion, "descripción", 500);
		unidadMedida = Textos.obligatorio(unidadMedida, "unidad de medida", 5);
	}

}
