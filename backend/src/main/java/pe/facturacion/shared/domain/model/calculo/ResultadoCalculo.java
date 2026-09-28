package pe.facturacion.shared.domain.model.calculo;

import java.util.List;

public record ResultadoCalculo(List<LineaCalculada> lineas, TotalesComprobante totales) {

	public ResultadoCalculo {
		lineas = List.copyOf(lineas);
	}

}
