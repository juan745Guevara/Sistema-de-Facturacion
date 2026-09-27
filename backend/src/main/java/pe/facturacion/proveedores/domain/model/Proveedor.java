package pe.facturacion.proveedores.domain.model;

import java.util.Objects;

import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Textos;

/** Emisor de los comprobantes de compra. {@code nombre} es la razón social cuando el documento es RUC. */
public record Proveedor(
		Long id,
		DocumentoIdentidad documento,
		String nombre,
		String direccion,
		String email,
		String telefono) {

	public Proveedor {
		Objects.requireNonNull(documento, "documento");
		nombre = Textos.obligatorio(nombre, "nombre", 200);
		direccion = Textos.opcional(direccion, "dirección", 200);
		email = Textos.opcional(email, "correo", 120);
		telefono = Textos.opcional(telefono, "teléfono", 20);
	}

	public Proveedor conId(Long nuevoId) {
		return new Proveedor(nuevoId, documento, nombre, direccion, email, telefono);
	}

}
