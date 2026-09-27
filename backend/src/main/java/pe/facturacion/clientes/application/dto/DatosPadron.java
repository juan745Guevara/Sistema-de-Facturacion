package pe.facturacion.clientes.application.dto;

import pe.facturacion.shared.domain.model.DocumentoIdentidad;

/** Los campos de ubicación, estado y condición solo llegan en las consultas de RUC. */
public record DatosPadron(
		DocumentoIdentidad documento,
		String nombre,
		String direccion,
		String ubigeo,
		String departamento,
		String provincia,
		String distrito,
		String estado,
		String condicion) {
}
