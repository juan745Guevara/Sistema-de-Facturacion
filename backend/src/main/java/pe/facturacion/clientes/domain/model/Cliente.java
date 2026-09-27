package pe.facturacion.clientes.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Textos;

/**
 * Persona o empresa a la que se emite comprobantes. {@code nombre} es la razón social si el documento es RUC, o
 * los nombres y apellidos en otro caso. Cada cliente tiene un único documento.
 */
public record Cliente(
		Long id,
		DocumentoIdentidad documento,
		String nombre,
		String direccion,
		String email,
		String telefono,
		LocalDate fechaNacimiento) {

	public Cliente {
		Objects.requireNonNull(documento, "documento");
		nombre = Textos.obligatorio(nombre, "nombre", 200);
		direccion = Textos.opcional(direccion, "dirección", 200);
		email = Textos.opcional(email, "correo", 120);
		telefono = Textos.opcional(telefono, "teléfono", 20);
	}

	public Cliente conId(Long nuevoId) {
		return new Cliente(nuevoId, documento, nombre, direccion, email, telefono, fechaNacimiento);
	}

}
