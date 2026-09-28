package pe.facturacion.sunat.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;

/** Un comprobante frente a SUNAT: su XML firmado, el CDR y el historial mínimo de envíos. */
public record DocumentoElectronico(
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
		byte[] xmlFirmado,
		byte[] cdr,
		int intentos,
		Instant ultimoEnvio) {

	public DocumentoElectronico {
		Objects.requireNonNull(tipo, "tipo");
		Objects.requireNonNull(serie, "serie");
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(estado, "estado");
		observaciones = observaciones == null ? List.of() : List.copyOf(observaciones);
	}

	public static DocumentoElectronico pendiente(TipoComprobante tipo, String serie, int correlativo,
			LocalDate fechaEmision) {
		return new DocumentoElectronico(null, tipo, serie, correlativo, fechaEmision, EstadoSunat.PENDIENTE, null,
				null, List.of(), null, null, null, 0, null);
	}

	/** {@code RUC-TT-SERIE-CORRELATIVO}, sin extensión, como exige SUNAT para el zip y el XML. */
	public String nombreArchivo(String rucEmisor) {
		return "%s-%s-%s-%d".formatted(rucEmisor, tipo.codigo(), serie, correlativo);
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

	public boolean firmado() {
		return xmlFirmado != null;
	}

	public DocumentoElectronico conXmlFirmado(byte[] xml, String hashFirma) {
		if (firmado()) {
			throw DominioException.conflicto("documento-ya-firmado",
					"El documento %s ya tiene XML firmado".formatted(numero()));
		}
		return new DocumentoElectronico(id, tipo, serie, correlativo, fechaEmision, estado, codigoRespuesta, mensaje,
				observaciones, Objects.requireNonNull(hashFirma), Objects.requireNonNull(xml), cdr, intentos,
				ultimoEnvio);
	}

	public DocumentoElectronico conRespuesta(RespuestaSunat respuesta, Instant momento) {
		return new DocumentoElectronico(id, tipo, serie, correlativo, fechaEmision, respuesta.estado(),
				respuesta.codigo(), respuesta.descripcion(), respuesta.observaciones(), hash, xmlFirmado,
				respuesta.cdr() != null ? respuesta.cdr() : cdr, intentos + 1, momento);
	}

	/** El envío no llegó a procesarse: se anota el intento y el motivo, pero el estado no cambia. */
	public DocumentoElectronico conErrorComunicacion(ErrorComunicacionSunat error, Instant momento) {
		return new DocumentoElectronico(id, tipo, serie, correlativo, fechaEmision, estado, error.codigoSunat(),
				error.getMessage(), observaciones, hash, xmlFirmado, cdr, intentos + 1, momento);
	}

	/** Estado resuelto por un resumen diario o una comunicación de baja. */
	public DocumentoElectronico conEstado(EstadoSunat nuevo, String codigo, String descripcion) {
		return new DocumentoElectronico(id, tipo, serie, correlativo, fechaEmision, nuevo, codigo, descripcion,
				observaciones, hash, xmlFirmado, cdr, intentos, ultimoEnvio);
	}

}
