package pe.facturacion.cotizaciones.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.Textos;

public record Cotizacion(
		Long id,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		Long clienteId,
		DocumentoIdentidad clienteDocumento,
		String clienteNombre,
		Moneda moneda,
		List<LineaCotizacion> lineas,
		BigDecimal gravadas,
		BigDecimal igv,
		BigDecimal total,
		String observacion) {

	public Cotizacion {
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (!serie.matches("[A-Z0-9]{4}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie debe tener 4 letras o dígitos");
		}
		if (correlativo < 1) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo debe ser mayor que cero");
		}
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(clienteDocumento, "clienteDocumento");
		clienteNombre = Textos.obligatorio(clienteNombre, "nombre del cliente", 200);
		moneda = moneda == null ? Moneda.PEN : moneda;
		lineas = List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "La cotización debe tener al menos una línea");
		}
		observacion = Textos.opcional(observacion, "observación", 500);
	}

	public Cotizacion conId(Long nuevoId) {
		return new Cotizacion(nuevoId, serie, correlativo, fechaEmision, clienteId, clienteDocumento, clienteNombre,
				moneda, lineas, gravadas, igv, total, observacion);
	}

}
