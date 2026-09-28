package pe.facturacion.ventas.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.ventas.domain.model.FormaPago;
import pe.facturacion.ventas.domain.model.Venta;

public interface EmitirVentaUseCase {

	Venta emitir(SolicitudEmision solicitud);

	/** Recalcula importes sin persistir. El frontend solo muestra este resultado. */
	ResultadoCalculo previsualizar(SolicitudCalculo solicitud);

	record ItemSolicitado(Long productoId, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuento,
			Boolean icbper) {
	}

	record CuotaSolicitada(LocalDate fechaPago, BigDecimal monto) {
	}

	record SolicitudCalculo(
			List<ItemSolicitado> items,
			DescuentoGlobal descuentoGlobal,
			Moneda moneda,
			BigDecimal tipoCambio) {
	}

	record SolicitudEmision(
			TipoComprobante tipo,
			String serie,
			Long clienteId,
			Moneda moneda,
			BigDecimal tipoCambio,
			LocalDate fechaVencimiento,
			DescuentoGlobal descuentoGlobal,
			FormaPago formaPago,
			List<CuotaSolicitada> cuotas,
			List<ItemSolicitado> items,
			String observacion) {
	}

}
