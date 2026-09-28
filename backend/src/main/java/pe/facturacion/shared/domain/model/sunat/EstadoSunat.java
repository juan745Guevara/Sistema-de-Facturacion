package pe.facturacion.shared.domain.model.sunat;

/** Situación de un documento electrónico frente a SUNAT. */
public enum EstadoSunat {

	/** Registrado, pero sin CDR: no se envió, falló la conexión o SUNAT devolvió una excepción (0100-1999). */
	PENDIENTE,
	/** Enviado en un resumen o baja cuyo ticket aún no se resuelve. */
	EN_PROCESO,
	ACEPTADO,
	/** Aceptado con observaciones (códigos 4000 en adelante). */
	OBSERVADO,
	/** Rechazado (códigos 2000-3999): el comprobante no tiene validez. */
	RECHAZADO,
	/** Dado de baja con comunicación de baja o resumen diario. */
	ANULADO,
	/** Documento interno que no se informa a SUNAT. */
	NO_APLICA;

	public boolean aceptado() {
		return this == ACEPTADO || this == OBSERVADO;
	}

	public boolean definitivo() {
		return this == ACEPTADO || this == OBSERVADO || this == RECHAZADO || this == ANULADO || this == NO_APLICA;
	}

}
