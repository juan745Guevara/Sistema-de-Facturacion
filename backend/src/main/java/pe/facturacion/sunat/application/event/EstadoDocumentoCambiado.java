package pe.facturacion.sunat.application.event;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

/**
 * Se publica dentro de la transacción que guarda el nuevo estado, para que el módulo dueño del comprobante
 * actualice el suyo (y devuelva stock si quedó rechazado o anulado) de forma atómica.
 */
public record EstadoDocumentoCambiado(
		TipoComprobante tipo,
		String serie,
		int correlativo,
		EstadoSunat estadoAnterior,
		EstadoSunat estado,
		String codigoRespuesta,
		String mensaje) {

	/** El comprobante dejó de tener validez tributaria. */
	public boolean invalidado() {
		return (estado == EstadoSunat.RECHAZADO || estado == EstadoSunat.ANULADO)
				&& estadoAnterior != EstadoSunat.RECHAZADO && estadoAnterior != EstadoSunat.ANULADO;
	}

}
