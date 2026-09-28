package pe.facturacion.sunat.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.sunat.domain.model.LineaLote;
import pe.facturacion.sunat.domain.model.TipoLote;

public record LoteDto(
		Long id,
		TipoLote tipo,
		String serie,
		int correlativo,
		LocalDate fechaReferencia,
		LocalDate fechaGeneracion,
		EstadoSunat estado,
		String ticket,
		String codigoRespuesta,
		String mensaje,
		int intentos,
		Instant ultimoEnvio,
		boolean tieneXml,
		boolean tieneCdr,
		List<LineaLote> lineas) {
}
