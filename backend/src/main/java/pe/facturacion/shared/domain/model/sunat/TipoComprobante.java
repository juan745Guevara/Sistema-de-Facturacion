package pe.facturacion.shared.domain.model.sunat;

import java.util.Arrays;

/**
 * Catálogo 01 de SUNAT más los documentos internos del sistema.
 * Los internos no se envían a SUNAT; su código solo tiene sentido dentro del sistema.
 */
public enum TipoComprobante {

	FACTURA("01", "Factura", true),
	BOLETA("03", "Boleta de venta", true),
	NOTA_CREDITO("07", "Nota de crédito", true),
	NOTA_DEBITO("08", "Nota de débito", true),
	GUIA_REMISION("09", "Guía de remisión remitente", true),
	RESUMEN_DIARIO("RC", "Resumen diario", true),
	COMUNICACION_BAJA("RA", "Comunicación de baja", true),
	NOTA_VENTA("NV", "Nota de venta", false),
	COTIZACION("CT", "Cotización", false);

	private final String codigo;
	private final String descripcion;
	private final boolean electronico;

	TipoComprobante(String codigo, String descripcion, boolean electronico) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.electronico = electronico;
	}

	public String codigo() {
		return codigo;
	}

	public String descripcion() {
		return descripcion;
	}

	public boolean electronico() {
		return electronico;
	}

	public static TipoComprobante desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(t -> t.codigo.equals(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tipo de comprobante desconocido: " + codigo));
	}

}
