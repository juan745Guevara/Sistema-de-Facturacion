package pe.facturacion.shared.domain.model.sunat;

import java.util.Arrays;

/** Catálogo 06 de SUNAT. */
public enum TipoDocumentoIdentidad {

	SIN_DOCUMENTO("0", "Sin documento", null),
	DNI("1", "DNI", 8),
	CARNET_EXTRANJERIA("4", "Carné de extranjería", null),
	RUC("6", "RUC", 11),
	PASAPORTE("7", "Pasaporte", null),
	CEDULA_DIPLOMATICA("A", "Cédula diplomática de identidad", null),
	DOC_PAIS_RESIDENCIA("B", "Documento de identidad del país de residencia (no domiciliado)", null),
	TIN("C", "Tax Identification Number (TIN)", null),
	IN("D", "Identification Number (IN)", null),
	TAM("E", "Tarjeta Andina de Migración", null);

	private final String codigo;
	private final String descripcion;
	private final Integer longitud;

	TipoDocumentoIdentidad(String codigo, String descripcion, Integer longitud) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.longitud = longitud;
	}

	public String codigo() {
		return codigo;
	}

	public String descripcion() {
		return descripcion;
	}

	/** Longitud fija exigida, o {@code null} si el documento no tiene una. */
	public Integer longitud() {
		return longitud;
	}

	public static TipoDocumentoIdentidad desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(t -> t.codigo.equalsIgnoreCase(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tipo de documento desconocido: " + codigo));
	}

}
