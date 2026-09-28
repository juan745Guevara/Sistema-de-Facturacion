package pe.facturacion.notas.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface EmitirNotaUseCase {

	Nota emitir(SolicitudEmision solicitud);

	ResultadoCalculo previsualizar(SolicitudCalculo solicitud);

	record ItemSolicitado(Long productoId, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuento,
			Boolean icbper) {
	}

	record SolicitudCalculo(
			TipoComprobante tipoReferencia,
			String serieReferencia,
			int correlativoReferencia,
			String codigoMotivo,
			List<ItemSolicitado> items,
			DescuentoGlobal descuentoGlobal) {
	}

	record SolicitudEmision(
			TipoComprobante tipo,
			String serie,
			TipoComprobante tipoReferencia,
			String serieReferencia,
			int correlativoReferencia,
			String codigoMotivo,
			List<ItemSolicitado> items,
			DescuentoGlobal descuentoGlobal,
			String observacion) {
	}

}
