package pe.facturacion.sunat.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;

public record LoteSunat(
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
		String hash,
		byte[] xmlFirmado,
		byte[] cdr,
		int intentos,
		Instant ultimoEnvio,
		List<LineaLote> lineas) {

	private static final DateTimeFormatter DIA = DateTimeFormatter.BASIC_ISO_DATE;

	public LoteSunat {
		Objects.requireNonNull(tipo, "tipo");
		serie = serie == null ? "" : serie.trim();
		if (!serie.matches("\\d{8}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie de un RC o RA es la fecha YYYYMMDD");
		}
		if (correlativo < 1) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo del lote debe ser mayor que cero");
		}
		Objects.requireNonNull(fechaReferencia, "fechaReferencia");
		Objects.requireNonNull(fechaGeneracion, "fechaGeneracion");
		Objects.requireNonNull(estado, "estado");
		lineas = lineas == null ? List.of() : List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("lote-vacio", "El lote debe incluir al menos un comprobante");
		}
	}

	public static LoteSunat nuevo(TipoLote tipo, LocalDate fechaReferencia, LocalDate fechaGeneracion, int correlativo,
			List<LineaLote> lineas) {
		return new LoteSunat(null, tipo, fechaGeneracion.format(DIA), correlativo, fechaReferencia, fechaGeneracion,
				EstadoSunat.PENDIENTE, null, null, null, null, null, null, 0, null, lineas);
	}

	public String numero() {
		return tipo.codigo() + "-" + serie + "-" + correlativo;
	}

	public String nombreArchivo(String rucEmisor) {
		return "%s-%s-%s-%d".formatted(rucEmisor, tipo.codigo(), serie, correlativo);
	}

	public boolean firmado() {
		return xmlFirmado != null;
	}

	public LoteSunat conId(Long nuevoId) {
		return new LoteSunat(nuevoId, tipo, serie, correlativo, fechaReferencia, fechaGeneracion, estado, ticket,
				codigoRespuesta, mensaje, hash, xmlFirmado, cdr, intentos, ultimoEnvio, lineas);
	}

	public LoteSunat conXmlFirmado(byte[] xml, String hashFirma) {
		return new LoteSunat(id, tipo, serie, correlativo, fechaReferencia, fechaGeneracion, estado, ticket,
				codigoRespuesta, mensaje, Objects.requireNonNull(hashFirma), Objects.requireNonNull(xml), cdr,
				intentos, ultimoEnvio, lineas);
	}

	public LoteSunat conTicket(String nuevoTicket, Instant momento) {
		return new LoteSunat(id, tipo, serie, correlativo, fechaReferencia, fechaGeneracion, EstadoSunat.EN_PROCESO,
				nuevoTicket, codigoRespuesta, mensaje, hash, xmlFirmado, cdr, intentos + 1, momento, lineas);
	}

	public LoteSunat conRespuesta(RespuestaSunat respuesta, Instant momento) {
		EstadoSunat nuevo = tipo == TipoLote.COMUNICACION_BAJA && respuesta.estado().aceptado()
				? EstadoSunat.ACEPTADO
				: respuesta.estado();
		return new LoteSunat(id, tipo, serie, correlativo, fechaReferencia, fechaGeneracion, nuevo, ticket,
				respuesta.codigo(), respuesta.descripcion(), hash, xmlFirmado,
				respuesta.cdr() != null ? respuesta.cdr() : cdr, intentos + 1, momento, lineas);
	}

	public LoteSunat conErrorComunicacion(ErrorComunicacionSunat error, Instant momento) {
		return new LoteSunat(id, tipo, serie, correlativo, fechaReferencia, fechaGeneracion, estado, ticket,
				error.codigoSunat(), error.getMessage(), hash, xmlFirmado, cdr, intentos + 1, momento, lineas);
	}

}
