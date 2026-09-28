package pe.facturacion.notas.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.Textos;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.MotivoNotaCredito;
import pe.facturacion.shared.domain.model.sunat.MotivoNotaDebito;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.ventas.domain.model.LineaVenta;

public record Nota(
		Long id,
		TipoComprobante tipo,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		LocalTime horaEmision,
		Moneda moneda,
		BigDecimal tipoCambio,
		Long clienteId,
		DocumentoIdentidad clienteDocumento,
		String clienteNombre,
		String clienteDireccion,
		ReferenciaComprobante referencia,
		String codigoMotivo,
		String descripcionMotivo,
		List<LineaVenta> lineas,
		TotalesComprobante totales,
		EstadoSunat estadoSunat,
		String observacion,
		boolean stockAplicado) {

	public Nota {
		Objects.requireNonNull(tipo, "tipo");
		if (tipo != TipoComprobante.NOTA_CREDITO && tipo != TipoComprobante.NOTA_DEBITO) {
			throw DominioException.reglaNegocio("tipo-invalido", "Solo se emiten notas de crédito o de débito");
		}
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (!serie.matches("[A-Z0-9]{4}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie debe tener 4 letras o dígitos");
		}
		if (correlativo < 1) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo debe ser mayor que cero");
		}
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(horaEmision, "horaEmision");
		Objects.requireNonNull(moneda, "moneda");
		tipoCambio = moneda == Moneda.PEN ? BigDecimal.ONE.setScale(3, RoundingMode.UNNECESSARY)
				: Objects.requireNonNull(tipoCambio, "tipoCambio").setScale(3, RoundingMode.HALF_UP);
		Objects.requireNonNull(clienteDocumento, "clienteDocumento");
		clienteNombre = Textos.obligatorio(clienteNombre, "nombre del cliente", 200);
		clienteDireccion = Textos.opcional(clienteDireccion, "dirección del cliente", 200);
		Objects.requireNonNull(referencia, "referencia");
		codigoMotivo = validarMotivo(tipo, codigoMotivo);
		descripcionMotivo = Textos.obligatorio(descripcionMotivo, "descripción del motivo", 250);
		lineas = List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "La nota debe tener al menos una línea");
		}
		Objects.requireNonNull(totales, "totales");
		estadoSunat = estadoSunat == null ? EstadoSunat.PENDIENTE : estadoSunat;
		observacion = Textos.opcional(observacion, "observación", 500);
		validarSerie(tipo, serie, referencia);
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

	public boolean esCredito() {
		return tipo == TipoComprobante.NOTA_CREDITO;
	}

	public boolean afectaStock() {
		return esCredito() && MotivoNotaCredito.desdeCodigo(codigoMotivo).afectaStock();
	}

	public Nota conId(Long nuevoId) {
		return new Nota(nuevoId, tipo, serie, correlativo, fechaEmision, horaEmision, moneda, tipoCambio, clienteId,
				clienteDocumento, clienteNombre, clienteDireccion, referencia, codigoMotivo, descripcionMotivo, lineas,
				totales, estadoSunat, observacion, stockAplicado);
	}

	public Nota conEstadoSunat(EstadoSunat nuevo) {
		return new Nota(id, tipo, serie, correlativo, fechaEmision, horaEmision, moneda, tipoCambio, clienteId,
				clienteDocumento, clienteNombre, clienteDireccion, referencia, codigoMotivo, descripcionMotivo, lineas,
				totales, nuevo, observacion, stockAplicado);
	}

	public Nota conStockAplicado(boolean aplicado) {
		return new Nota(id, tipo, serie, correlativo, fechaEmision, horaEmision, moneda, tipoCambio, clienteId,
				clienteDocumento, clienteNombre, clienteDireccion, referencia, codigoMotivo, descripcionMotivo, lineas,
				totales, estadoSunat, observacion, aplicado);
	}

	private static String validarMotivo(TipoComprobante tipo, String codigo) {
		if (codigo == null || codigo.isBlank()) {
			throw DominioException.reglaNegocio("motivo-obligatorio", "La nota exige un motivo del catálogo SUNAT");
		}
		String valor = codigo.trim();
		try {
			if (tipo == TipoComprobante.NOTA_CREDITO) {
				MotivoNotaCredito.desdeCodigo(valor);
			} else {
				MotivoNotaDebito.desdeCodigo(valor);
			}
		} catch (IllegalArgumentException e) {
			throw DominioException.reglaNegocio("motivo-invalido", e.getMessage());
		}
		return valor;
	}

	private static void validarSerie(TipoComprobante tipo, String serie, ReferenciaComprobante referencia) {
		char prefijo = referencia.tipo() == TipoComprobante.FACTURA ? 'F' : 'B';
		if (serie.charAt(0) != prefijo) {
			throw DominioException.reglaNegocio("serie-invalida",
					"La serie de la nota de una %s debe empezar con %s".formatted(referencia.tipo().descripcion(), prefijo));
		}
	}

}
