package pe.facturacion.cotizaciones.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import pe.facturacion.cotizaciones.domain.model.Cotizacion;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GestionarCotizacionesUseCase {

	Cotizacion emitir(Solicitud solicitud);

	Cotizacion obtener(Long id);

	Pagina<Cotizacion> buscar(ConsultaPaginada consulta);

	record Item(Long productoId, BigDecimal cantidad, BigDecimal precioUnitario) {
	}

	record Solicitud(String serie, Long clienteId, List<Item> items, String observacion) {
	}

}
