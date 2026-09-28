package pe.facturacion.sunat.application.dto;

import java.time.LocalDate;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

/** Criterios opcionales: los {@code null} no filtran. */
public record FiltroDocumentos(TipoComprobante tipo, EstadoSunat estado, LocalDate desde, LocalDate hasta) {

	public static FiltroDocumentos todos() {
		return new FiltroDocumentos(null, null, null, null);
	}

}
