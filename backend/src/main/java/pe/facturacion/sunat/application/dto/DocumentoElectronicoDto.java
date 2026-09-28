package pe.facturacion.sunat.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public record DocumentoElectronicoDto(
		Long id,
		TipoComprobante tipo,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		EstadoSunat estado,
		String codigoRespuesta,
		String mensaje,
		List<String> observaciones,
		String hash,
		int intentos,
		Instant ultimoEnvio,
		boolean tieneXml,
		boolean tieneCdr) {
}
