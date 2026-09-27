package pe.facturacion.catalogo.domain.model;

import java.util.Objects;

/** Unidad del catálogo 03 de SUNAT (UN/ECE rec 20). Solo las activas se ofrecen al registrar productos. */
public record UnidadMedida(String codigo, String descripcion, boolean activa) {

	public UnidadMedida {
		Objects.requireNonNull(codigo, "codigo");
		Objects.requireNonNull(descripcion, "descripcion");
	}

	public UnidadMedida conActivacion(boolean activar) {
		return new UnidadMedida(codigo, descripcion, activar);
	}

}
