package pe.facturacion.notas.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.facturacion.notas.application.port.in.EmitirNotaUseCase.ItemSolicitado;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase.SolicitudCalculo;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase.SolicitudEmision;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.ventas.domain.model.LineaVenta;

final class NotasDtos {

	private NotasDtos() {
	}

	record ItemRequest(
			@NotNull Long productoId,
			@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
			@DecimalMin(value = "0", inclusive = false) BigDecimal precioUnitario,
			@DecimalMin("0") BigDecimal descuento,
			Boolean icbper) {
	}

	record DescuentoRequest(DescuentoGlobal.Tipo tipo, @DecimalMin("0") BigDecimal valor) {
	}

	record CalculoRequest(
			@NotNull TipoComprobante tipoReferencia,
			@NotNull @Size(min = 4, max = 4) String serieReferencia,
			@NotNull int correlativoReferencia,
			@NotNull String codigoMotivo,
			List<@Valid ItemRequest> items,
			@Valid DescuentoRequest descuentoGlobal) {
	}

	record EmisionRequest(
			@NotNull TipoComprobante tipo,
			@NotNull @Size(min = 4, max = 4) String serie,
			@NotNull TipoComprobante tipoReferencia,
			@NotNull @Size(min = 4, max = 4) String serieReferencia,
			@NotNull int correlativoReferencia,
			@NotNull String codigoMotivo,
			List<@Valid ItemRequest> items,
			@Valid DescuentoRequest descuentoGlobal,
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
			BigDecimal importe) {

		static LineaResponse desde(LineaVenta linea) {
			LineaCalculada c = linea.calculo();
			return new LineaResponse(linea.productoId(), linea.codigo(), linea.descripcion(), linea.unidadMedida(),
					c.cantidad(), c.afectacion(), c.precioUnitario(), c.importe());
		}
	}

	record NotaResponse(
			Long id,
			TipoComprobante tipo,
			String serie,
			int correlativo,
			LocalDate fechaEmision,
			LocalTime horaEmision,
			Moneda moneda,
			Long clienteId,
			TipoDocumentoIdentidad clienteTipoDocumento,
			String clienteNumeroDocumento,
			String clienteNombre,
			TipoComprobante tipoReferencia,
			String serieReferencia,
			int correlativoReferencia,
			String codigoMotivo,
			String descripcionMotivo,
			List<LineaResponse> lineas,
			TotalesComprobante totales,
			EstadoSunat estadoSunat,
			String observacion) {

		static NotaResponse desde(Nota nota) {
			return new NotaResponse(nota.id(), nota.tipo(), nota.serie(), nota.correlativo(), nota.fechaEmision(),
					nota.horaEmision(), nota.moneda(), nota.clienteId(), nota.clienteDocumento().tipo(),
					nota.clienteDocumento().numero(), nota.clienteNombre(), nota.referencia().tipo(),
					nota.referencia().serie(), nota.referencia().correlativo(), nota.codigoMotivo(),
					nota.descripcionMotivo(), nota.lineas().stream().map(LineaResponse::desde).toList(),
					nota.totales(), nota.estadoSunat(), nota.observacion());
		}
	}

	record CalculoResponse(List<LineaCalculada> lineas, TotalesComprobante totales) {

		static CalculoResponse desde(ResultadoCalculo resultado) {
			return new CalculoResponse(resultado.lineas(), resultado.totales());
		}
	}

	static SolicitudCalculo aCalculo(CalculoRequest request) {
		return new SolicitudCalculo(request.tipoReferencia(), request.serieReferencia(), request.correlativoReferencia(),
				request.codigoMotivo(), items(request.items()), descuento(request.descuentoGlobal()));
	}

	static SolicitudEmision aEmision(EmisionRequest request) {
		return new SolicitudEmision(request.tipo(), request.serie(), request.tipoReferencia(),
				request.serieReferencia(), request.correlativoReferencia(), request.codigoMotivo(),
				items(request.items()), descuento(request.descuentoGlobal()), request.observacion());
	}

	private static List<ItemSolicitado> items(List<ItemRequest> items) {
		if (items == null) {
			return List.of();
		}
		return items.stream()
				.map(i -> new ItemSolicitado(i.productoId(), i.cantidad(), i.precioUnitario(), i.descuento(), i.icbper()))
				.toList();
	}

	private static DescuentoGlobal descuento(DescuentoRequest request) {
		return request == null ? DescuentoGlobal.ninguno() : new DescuentoGlobal(request.tipo(), request.valor());
	}

}
