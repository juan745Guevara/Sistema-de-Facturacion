package pe.facturacion.compras.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface RegistrarCompraUseCase {

	Compra registrar(Solicitud solicitud);

	Compra anular(Long id);

	Compra obtener(Long id);

	Pagina<Compra> buscar(ConsultaPaginada consulta);

	record Item(Long productoId, BigDecimal cantidad, BigDecimal precioUnitario) {
	}

	record Solicitud(
			TipoComprobante tipo,
			String serie,
			String correlativo,
			LocalDate fechaEmision,
			Long proveedorId,
			List<Item> items,
			String observacion) {
	}

}
