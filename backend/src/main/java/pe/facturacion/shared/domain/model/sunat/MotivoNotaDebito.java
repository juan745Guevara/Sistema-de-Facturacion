package pe.facturacion.shared.domain.model.sunat;

import java.util.Arrays;

/** Catálogo 10: tipo de nota de débito. */
public enum MotivoNotaDebito {

	INTERESES("01", "Intereses por mora"),
	AUMENTO_VALOR("02", "Aumento en el valor"),
	PENALIDADES("03", "Penalidades / otros conceptos"),
	AJUSTES_EXPORTACION("10", "Ajustes de operaciones de exportación"),
	AJUSTES_IVAP("11", "Ajustes afectos al IVAP");

	private final String codigo;
	private final String descripcion;

	MotivoNotaDebito(String codigo, String descripcion) {
		this.codigo = codigo;
		this.descripcion = descripcion;
	}

	public String codigo() {
		return codigo;
	}

	public String descripcion() {
		return descripcion;
	}

	public static MotivoNotaDebito desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(m -> m.codigo.equals(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Motivo de nota de débito desconocido: " + codigo));
	}

}
