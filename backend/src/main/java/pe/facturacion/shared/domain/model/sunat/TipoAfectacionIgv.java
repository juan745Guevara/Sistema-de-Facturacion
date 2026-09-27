package pe.facturacion.shared.domain.model.sunat;

import java.util.Arrays;

/** Catálogo 07 de SUNAT, con el tributo del catálogo 05 que le corresponde en el XML. */
public enum TipoAfectacionIgv {

	GRAVADO_ONEROSO("10", "Gravado - Operación onerosa", Tributo.IGV, false),
	GRAVADO_RETIRO_PREMIO("11", "Gravado - Retiro por premio", Tributo.GRATUITO, true),
	GRAVADO_RETIRO_DONACION("12", "Gravado - Retiro por donación", Tributo.GRATUITO, true),
	GRAVADO_RETIRO("13", "Gravado - Retiro", Tributo.GRATUITO, true),
	GRAVADO_RETIRO_PUBLICIDAD("14", "Gravado - Retiro por publicidad", Tributo.GRATUITO, true),
	GRAVADO_BONIFICACIONES("15", "Gravado - Bonificaciones", Tributo.GRATUITO, true),
	GRAVADO_RETIRO_TRABAJADORES("16", "Gravado - Retiro por entrega a trabajadores", Tributo.GRATUITO, true),
	GRAVADO_IVAP("17", "Gravado - IVAP", Tributo.IVAP, false),
	EXONERADO_ONEROSO("20", "Exonerado - Operación onerosa", Tributo.EXONERADO, false),
	EXONERADO_GRATUITO("21", "Exonerado - Transferencia gratuita", Tributo.GRATUITO, true),
	INAFECTO_ONEROSO("30", "Inafecto - Operación onerosa", Tributo.INAFECTO, false),
	INAFECTO_RETIRO_BONIFICACION("31", "Inafecto - Retiro por bonificación", Tributo.GRATUITO, true),
	INAFECTO_RETIRO("32", "Inafecto - Retiro", Tributo.GRATUITO, true),
	INAFECTO_RETIRO_MUESTRAS_MEDICAS("33", "Inafecto - Retiro por muestras médicas", Tributo.GRATUITO, true),
	INAFECTO_RETIRO_CONVENIO_COLECTIVO("34", "Inafecto - Retiro por convenio colectivo", Tributo.GRATUITO, true),
	INAFECTO_RETIRO_PREMIO("35", "Inafecto - Retiro por premio", Tributo.GRATUITO, true),
	INAFECTO_RETIRO_PUBLICIDAD("36", "Inafecto - Retiro por publicidad", Tributo.GRATUITO, true),
	EXPORTACION("40", "Exportación de bienes o servicios", Tributo.EXPORTACION, false);

	/** Catálogo 05 de SUNAT. */
	public enum Tributo {
		IGV("1000", "IGV", "VAT"),
		IVAP("1016", "IVAP", "VAT"),
		EXPORTACION("9995", "EXP", "FRE"),
		GRATUITO("9996", "GRA", "FRE"),
		EXONERADO("9997", "EXO", "VAT"),
		INAFECTO("9998", "INA", "FRE");

		private final String codigo;
		private final String nombre;
		private final String codigoInternacional;

		Tributo(String codigo, String nombre, String codigoInternacional) {
			this.codigo = codigo;
			this.nombre = nombre;
			this.codigoInternacional = codigoInternacional;
		}

		public String codigo() {
			return codigo;
		}

		public String nombre() {
			return nombre;
		}

		public String codigoInternacional() {
			return codigoInternacional;
		}
	}

	private final String codigo;
	private final String descripcion;
	private final Tributo tributo;
	private final boolean gratuito;

	TipoAfectacionIgv(String codigo, String descripcion, Tributo tributo, boolean gratuito) {
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.tributo = tributo;
		this.gratuito = gratuito;
	}

	public String codigo() {
		return codigo;
	}

	public String descripcion() {
		return descripcion;
	}

	public Tributo tributo() {
		return tributo;
	}

	public boolean gratuito() {
		return gratuito;
	}

	/** Operación gravada con IGV: onerosa (10) o gratuita (11 a 16). */
	public boolean gravado() {
		return tributo == Tributo.IGV || (gratuito && codigo.startsWith("1"));
	}

	public static TipoAfectacionIgv desdeCodigo(String codigo) {
		return Arrays.stream(values())
				.filter(t -> t.codigo.equals(codigo))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tipo de afectación desconocido: " + codigo));
	}

}
