package pe.facturacion.guias.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.guias.domain.model.Guia;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GestionarGuiasUseCase {

	Guia emitir(Solicitud solicitud);

	Guia obtener(Long id);

	Pagina<Guia> buscar(ConsultaPaginada consulta);

	record Item(Long productoId, BigDecimal cantidad) {
	}

	record Solicitud(
			String serie,
			Long clienteId,
			String motivoTraslado,
			String modalidad,
			LocalDate fechaTraslado,
			BigDecimal pesoTotal,
			int bultos,
			String ubigeoPartida,
			String direccionPartida,
			String ubigeoLlegada,
			String direccionLlegada,
			String transportistaDocumento,
			String transportistaNombre,
			String placa,
			String licencia,
			List<Item> items,
			String observacion) {
	}

}
