package pe.facturacion.ventas.domain.model;

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
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

public record Venta(
		Long id,
		TipoComprobante tipo,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		LocalTime horaEmision,
		LocalDate fechaVencimiento,
		Moneda moneda,
		BigDecimal tipoCambio,
		Long clienteId,
		DocumentoIdentidad clienteDocumento,
		String clienteNombre,
		String clienteDireccion,
		List<LineaVenta> lineas,
		TotalesComprobante totales,
		FormaPago formaPago,
		List<Cuota> cuotas,
		boolean bienesSelva,
		boolean serviciosSelva,
		EstadoSunat estadoSunat,
		String observacion,
		boolean stockDevuelto) {

	private static final BigDecimal TOPE_BOLETA_SIN_DOCUMENTO = new BigDecimal("700.00");

	public Venta {
		Objects.requireNonNull(tipo, "tipo");
		if (tipo != TipoComprobante.FACTURA && tipo != TipoComprobante.BOLETA && tipo != TipoComprobante.NOTA_VENTA) {
			throw DominioException.reglaNegocio("tipo-invalido", "Solo se emiten facturas, boletas o notas de venta");
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
		tipoCambio = validarTipoCambio(moneda, tipoCambio);
		Objects.requireNonNull(clienteDocumento, "clienteDocumento");
		clienteNombre = Textos.obligatorio(clienteNombre, "nombre del cliente", 200);
		clienteDireccion = Textos.opcional(clienteDireccion, "dirección del cliente", 200);
		lineas = List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "El comprobante debe tener al menos una línea");
		}
		Objects.requireNonNull(totales, "totales");
		formaPago = formaPago == null ? FormaPago.CONTADO : formaPago;
		cuotas = cuotas == null ? List.of() : List.copyOf(cuotas);
		estadoSunat = estadoSunat == null
				? (tipo.electronico() ? EstadoSunat.PENDIENTE : EstadoSunat.NO_APLICA)
				: estadoSunat;
		observacion = Textos.opcional(observacion, "observación", 500);
		validarCliente(tipo, clienteDocumento, totales.total());
		validarPago(formaPago, cuotas, totales, fechaEmision);
		validarSelva(bienesSelva, serviciosSelva, totales);
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

	public Venta conId(Long nuevoId) {
		return new Venta(nuevoId, tipo, serie, correlativo, fechaEmision, horaEmision, fechaVencimiento, moneda,
				tipoCambio, clienteId, clienteDocumento, clienteNombre, clienteDireccion, lineas, totales, formaPago,
				cuotas, bienesSelva, serviciosSelva, estadoSunat, observacion, stockDevuelto);
	}

	public Venta conEstadoSunat(EstadoSunat nuevo) {
		return new Venta(id, tipo, serie, correlativo, fechaEmision, horaEmision, fechaVencimiento, moneda, tipoCambio,
				clienteId, clienteDocumento, clienteNombre, clienteDireccion, lineas, totales, formaPago, cuotas,
				bienesSelva, serviciosSelva, nuevo, observacion, stockDevuelto);
	}

	public Venta conStockDevuelto() {
		return new Venta(id, tipo, serie, correlativo, fechaEmision, horaEmision, fechaVencimiento, moneda, tipoCambio,
				clienteId, clienteDocumento, clienteNombre, clienteDireccion, lineas, totales, formaPago, cuotas,
				bienesSelva, serviciosSelva, estadoSunat, observacion, true);
	}

	private void validarCliente(TipoComprobante tipo, DocumentoIdentidad documento, BigDecimal total) {
		if (tipo == TipoComprobante.FACTURA && !documento.esRuc()) {
			throw DominioException.reglaNegocio("factura-sin-ruc", "La factura exige un cliente con RUC");
		}
		if (tipo == TipoComprobante.BOLETA && documento.tipo() == TipoDocumentoIdentidad.SIN_DOCUMENTO
				&& total.compareTo(TOPE_BOLETA_SIN_DOCUMENTO) >= 0) {
			throw DominioException.reglaNegocio("boleta-sin-documento",
					"Una boleta de S/ 700 o más exige DNI u otro documento de identidad");
		}
	}

	private static void validarPago(FormaPago formaPago, List<Cuota> cuotas, TotalesComprobante totales,
			LocalDate fechaEmision) {
		if (formaPago == FormaPago.CONTADO) {
			if (!cuotas.isEmpty()) {
				throw DominioException.reglaNegocio("cuotas-en-contado", "El pago al contado no lleva cuotas");
			}
			return;
		}
		if (cuotas.isEmpty()) {
			throw DominioException.reglaNegocio("credito-sin-cuotas", "El crédito debe indicar al menos una cuota");
		}
		BigDecimal suma = cuotas.stream().map(Cuota::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (suma.compareTo(totales.total()) != 0) {
			throw DominioException.reglaNegocio("cuotas-descuadradas",
					"La suma de las cuotas debe ser igual al total del comprobante");
		}
		for (Cuota cuota : cuotas) {
			if (!cuota.fechaPago().isAfter(fechaEmision)) {
				throw DominioException.reglaNegocio("cuota-invalida",
						"La fecha de cada cuota debe ser posterior a la emisión");
			}
		}
	}

	private static void validarSelva(boolean bienesSelva, boolean serviciosSelva, TotalesComprobante totales) {
		if ((bienesSelva || serviciosSelva) && totales.gravadas().signum() > 0) {
			throw DominioException.reglaNegocio("selva-con-gravadas",
					"La leyenda de la Amazonía no aplica si hay operaciones gravadas (error SUNAT 3284)");
		}
	}

	private static BigDecimal validarTipoCambio(Moneda moneda, BigDecimal tipoCambio) {
		if (moneda == Moneda.PEN) {
			return BigDecimal.ONE.setScale(3, RoundingMode.UNNECESSARY);
		}
		if (tipoCambio == null || tipoCambio.signum() <= 0) {
			throw DominioException.reglaNegocio("tipo-cambio-invalido",
					"El tipo de cambio es obligatorio cuando la moneda es dólares");
		}
		return tipoCambio.setScale(3, RoundingMode.HALF_UP);
	}

}
