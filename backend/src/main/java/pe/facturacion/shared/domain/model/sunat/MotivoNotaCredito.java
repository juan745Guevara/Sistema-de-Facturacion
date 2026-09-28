package pe.facturacion.shared.domain.model.sunat;

import java.util.Arrays;

/** Catálogo 09: tipo de nota de crédito. */
public enum MotivoNotaCredito {

	ANULACION_OPERACION("01", "Anulación de la operación", true),
	ANULACION_ERROR_RUC("02", "Anulación por error en el RUC", false),
	CORRECCION_DESCRIPCION("03", "Corrección por error en la descripción", false),
	DESCUENTO_GLOBAL("04", "Descuento global", false),
	DESCUENTO_ITEM("05", "Descuento por ítem", false),
	DEVOLUCION_TOTAL("06", "Devolución total", true),
	DEVOLUCION_ITEM("07", "Devolución por ítem", true),
	BONIFICACION("08", "Bonificación", false),
	DISMINUCION_VALOR("09", "Disminución en el valor", false),
	OTROS("10", "Otros conceptos", false),
	AJUSTES_EXPORTACION("11", "Ajustes de operaciones de exportación", false),
	AJUSTES_IVAP("12", "Ajustes afectos al IVAP", false),
	AJUSTES_MONTOS_FECHAS("13", "Ajustes de montos y/o fechas de pago", false);

	private final String codigo;
	private final String descripcion;
	private final boolean afectaStock;

	MotivoNotaCredito(String codigo, String descripcion, boolean afectaStock) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.afectaStock = afectaStock;
	}

	public String codigo() {
		return codigo;
	}

	public String descripcion() {
		return descripcion;
	}

	public boolean afectaStock() {
		return afectaStock;
	}

	public boolean copiaComprobanteCompleto() {
		return this == ANULACION_OPERACION || this == ANULACION_ERROR_RUC || this == DEVOLUCION_TOTAL;
	}

	public static MotivoNotaCredito desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(m -> m.codigo.equals(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Motivo de nota de crédito desconocido: " + codigo));
	}

}
