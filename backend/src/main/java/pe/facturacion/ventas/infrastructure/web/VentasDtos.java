package pe.facturacion.ventas.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.CuotaSolicitada;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.ItemSolicitado;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.SolicitudCalculo;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.SolicitudEmision;
import pe.facturacion.ventas.domain.model.FormaPago;
import pe.facturacion.ventas.domain.model.Venta;

final class VentasDtos {

	private VentasDtos() {
	}

	record ItemRequest(
			@NotNull Long productoId,
			@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
			@DecimalMin(value = "0", inclusive = false) BigDecimal precioUnitario,
			@DecimalMin("0") BigDecimal descuento,
			Boolean icbper) {
	}

	record CuotaRequest(@NotNull LocalDate fechaPago, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto) {
	}

	record DescuentoRequest(DescuentoGlobal.Tipo tipo, @DecimalMin("0") BigDecimal valor) {
	}

	record CalculoRequest(
			@NotEmpty List<@Valid ItemRequest> items,
			@Valid DescuentoRequest descuentoGlobal,
			Moneda moneda,
			BigDecimal tipoCambio) {
	}

	record EmisionRequest(
			@NotNull TipoComprobante tipo,
			@NotNull @Size(min = 4, max = 4) String serie,
			@NotNull Long clienteId,
			Moneda moneda,
			BigDecimal tipoCambio,
			LocalDate fechaVencimiento,
			@Valid DescuentoRequest descuentoGlobal,
			FormaPago formaPago,
			List<@Valid CuotaRequest> cuotas,
			@NotEmpty List<@Valid ItemRequest> items,
			@Size(max = 500) String observacion) {
	}

	record LineaResponse(
			Long productoId,
			String codigo,
			String descripcion,
			String unidadMedida,
			BigDecimal cantidad,
			TipoAfectacionIgv tipoAfectacionIgv,
			BigDecimal precioUnitario,
			BigDecimal valorUnitario,
			BigDecimal descuento,
			BigDecimal valorVenta,
			BigDecimal impuesto,
			BigDecimal icbper,
			BigDecimal importe) {

		static LineaResponse desde(pe.facturacion.ventas.domain.model.LineaVenta linea) {
			LineaCalculada c = linea.calculo();
			return new LineaResponse(linea.productoId(), linea.codigo(), linea.descripcion(), linea.unidadMedida(),
					c.cantidad(), c.afectacion(), c.precioUnitario(), c.valorUnitario(), c.descuento(), c.valorVenta(),
					c.impuesto(), c.icbper(), c.importe());
		}
	}

	record VentaResponse(
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
			TipoDocumentoIdentidad clienteTipoDocumento,
			String clienteNumeroDocumento,
			String clienteNombre,
			String clienteDireccion,
			List<LineaResponse> lineas,
			TotalesComprobante totales,
			FormaPago formaPago,
			List<pe.facturacion.ventas.domain.model.Cuota> cuotas,
			boolean bienesSelva,
			boolean serviciosSelva,
			EstadoSunat estadoSunat,
			String observacion) {

		static VentaResponse desde(Venta venta) {
			return new VentaResponse(venta.id(), venta.tipo(), venta.serie(), venta.correlativo(), venta.fechaEmision(),
					venta.horaEmision(), venta.fechaVencimiento(), venta.moneda(), venta.tipoCambio(), venta.clienteId(),
					venta.clienteDocumento().tipo(), venta.clienteDocumento().numero(), venta.clienteNombre(),
					venta.clienteDireccion(), venta.lineas().stream().map(LineaResponse::desde).toList(),
					venta.totales(), venta.formaPago(), venta.cuotas(), venta.bienesSelva(), venta.serviciosSelva(),
					venta.estadoSunat(), venta.observacion());
		}
	}

	record CalculoResponse(List<LineaCalculada> lineas, TotalesComprobante totales) {

		static CalculoResponse desde(ResultadoCalculo resultado) {
			return new CalculoResponse(resultado.lineas(), resultado.totales());
		}
	}

	static SolicitudCalculo aCalculo(CalculoRequest request) {
		return new SolicitudCalculo(items(request.items()), descuento(request.descuentoGlobal()), request.moneda(),
				request.tipoCambio());
	}

	static SolicitudEmision aEmision(EmisionRequest request) {
		List<CuotaSolicitada> cuotas = request.cuotas() == null ? List.of()
				: request.cuotas().stream().map(c -> new CuotaSolicitada(c.fechaPago(), c.monto())).toList();
		return new SolicitudEmision(request.tipo(), request.serie(), request.clienteId(), request.moneda(),
				request.tipoCambio(), request.fechaVencimiento(), descuento(request.descuentoGlobal()),
				request.formaPago(), cuotas, items(request.items()), request.observacion());
	}

	private static List<ItemSolicitado> items(List<ItemRequest> items) {
		return items.stream()
				.map(i -> new ItemSolicitado(i.productoId(), i.cantidad(), i.precioUnitario(), i.descuento(), i.icbper()))
				.toList();
	}

	private static DescuentoGlobal descuento(DescuentoRequest request) {
		return request == null ? DescuentoGlobal.ninguno() : new DescuentoGlobal(request.tipo(), request.valor());
	}

}
