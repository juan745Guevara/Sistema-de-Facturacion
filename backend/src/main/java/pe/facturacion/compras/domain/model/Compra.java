package pe.facturacion.compras.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.Textos;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public record Compra(
		Long id,
		TipoComprobante tipo,
		String serie,
		String correlativo,
		LocalDate fechaEmision,
		Long proveedorId,
		DocumentoIdentidad proveedorDocumento,
		String proveedorNombre,
		Moneda moneda,
		List<LineaCompra> lineas,
		BigDecimal gravadas,
		BigDecimal igv,
		BigDecimal total,
		boolean anulada,
		String observacion) {

	public Compra {
		Objects.requireNonNull(tipo, "tipo");
		if (tipo != TipoComprobante.FACTURA && tipo != TipoComprobante.BOLETA && tipo != TipoComprobante.NOTA_VENTA) {
			throw DominioException.reglaNegocio("tipo-invalido", "La compra solo admite factura, boleta o nota de venta");
		}
		serie = Textos.obligatorio(serie, "serie", 20).toUpperCase();
		correlativo = Textos.obligatorio(correlativo, "número", 20);
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(proveedorDocumento, "proveedorDocumento");
		proveedorNombre = Textos.obligatorio(proveedorNombre, "nombre del proveedor", 200);
		moneda = moneda == null ? Moneda.PEN : moneda;
		lineas = List.copyOf(lineas);
		if (lineas.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "La compra debe tener al menos una línea");
		}
		observacion = Textos.opcional(observacion, "observación", 500);
	}

	public Compra conId(Long nuevoId) {
		return new Compra(nuevoId, tipo, serie, correlativo, fechaEmision, proveedorId, proveedorDocumento,
				proveedorNombre, moneda, lineas, gravadas, igv, total, anulada, observacion);
	}

	public Compra marcarAnulada() {
		if (anulada) {
			throw DominioException.conflicto("compra-ya-anulada", "La compra ya está anulada");
		}
		return new Compra(id, tipo, serie, correlativo, fechaEmision, proveedorId, proveedorDocumento, proveedorNombre,
				moneda, lineas, gravadas, igv, total, true, observacion);
	}

}
