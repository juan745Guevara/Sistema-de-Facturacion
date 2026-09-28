package pe.facturacion.sunat.domain.model;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public enum TipoLote {

	RESUMEN_DIARIO(TipoComprobante.RESUMEN_DIARIO),
	COMUNICACION_BAJA(TipoComprobante.COMUNICACION_BAJA);

	private final TipoComprobante tipoComprobante;

	TipoLote(TipoComprobante tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public TipoComprobante tipoComprobante() {
		return tipoComprobante;
	}

	public String codigo() {
		return tipoComprobante.codigo();
	}

}
