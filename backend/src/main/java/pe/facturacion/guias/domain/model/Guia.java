package pe.facturacion.guias.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Textos;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;

public record Guia(
		Long id,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		Long clienteId,
		DocumentoIdentidad destinatario,
		String destinatarioNombre,
		String motivoTraslado,
		String modalidad,
		LocalDate fechaTraslado,
		BigDecimal pesoTotal,
		int bultos,
		String ubigeoPartida,
		String direccionPartida,
		String ubigeoLlegada,
		String direccionLlegada,
		String transportistaDocumento,
		String transportistaNombre,
		String placa,
		String licencia,
		EstadoSunat estadoSunat,
		String ticket,
		String observacion,
		List<LineaGuia> lineas) {

	public Guia {
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (!serie.matches("T[A-Z0-9]{3}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie de la guía debe empezar con T y tener 4 caracteres");
		}
		if (correlativo < 1) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo debe ser mayor que cero");
		}
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(destinatario, "destinatario");
		destinatarioNombre = Textos.obligatorio(destinatarioNombre, "destinatario", 200);
		motivoTraslado = Textos.obligatorio(motivoTraslado, "motivo de traslado", 2);
		modalidad = Textos.obligatorio(modalidad, "modalidad", 2);
		Objects.requireNonNull(fechaTraslado, "fechaTraslado");
		if (pesoTotal == null || pesoTotal.signum() <= 0) {
			throw DominioException.reglaNegocio("peso-invalido", "El peso total debe ser mayor que cero");
		}
		if (bultos < 1) {
			throw DominioException.reglaNegocio("bultos-invalidos", "Debe haber al menos un bulto");
		}
		ubigeoPartida = Textos.obligatorio(ubigeoPartida, "ubigeo de partida", 6);
		direccionPartida = Textos.obligatorio(direccionPartida, "dirección de partida", 200);
		ubigeoLlegada = Textos.obligatorio(ubigeoLlegada, "ubigeo de llegada", 6);
		direccionLlegada = Textos.obligatorio(direccionLlegada, "dirección de llegada", 200);
		estadoSunat = estadoSunat == null ? EstadoSunat.PENDIENTE : estadoSunat;
		observacion = Textos.opcional(observacion, "observación", 500);
		lineas = List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "La guía debe tener al menos una línea");
		}
	}

	public Guia conId(Long nuevoId) {
		return new Guia(nuevoId, serie, correlativo, fechaEmision, clienteId, destinatario, destinatarioNombre,
				motivoTraslado, modalidad, fechaTraslado, pesoTotal, bultos, ubigeoPartida, direccionPartida,
				ubigeoLlegada, direccionLlegada, transportistaDocumento, transportistaNombre, placa, licencia,
				estadoSunat, ticket, observacion, lineas);
	}

}
