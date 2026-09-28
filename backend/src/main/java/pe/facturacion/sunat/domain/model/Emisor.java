package pe.facturacion.sunat.domain.model;

import java.util.Objects;

/** Datos del emisor que van en cada XML. */
public record Emisor(
		String ruc,
		String razonSocial,
		String nombreComercial,
		String direccion,
		String ubigeo,
		String departamento,
		String provincia,
		String distrito,
		String codigoPais,
		String codigoEstablecimiento) {

	public Emisor {
		Objects.requireNonNull(ruc, "ruc");
		Objects.requireNonNull(razonSocial, "razonSocial");
	}

}
