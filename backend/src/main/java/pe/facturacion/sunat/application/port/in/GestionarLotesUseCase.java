package pe.facturacion.sunat.application.port.in;

import java.time.LocalDate;
import java.util.List;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.LoteDto;
import pe.facturacion.sunat.domain.model.TipoLote;

public interface GestionarLotesUseCase {

	/** Boletas del día que aún no se informaron (pendientes de resumen). */
	List<DocumentoPendiente> previsualizarResumen(LocalDate fecha);

	LoteDto generarResumen(LocalDate fecha);

	LoteDto darBaja(TipoComprobante tipo, String serie, int correlativo, String motivo);

	LoteDto consultarTicket(Long loteId);

	Pagina<LoteDto> buscar(TipoLote tipo, ConsultaPaginada consulta);

	LoteDto obtener(Long id);

	record DocumentoPendiente(TipoComprobante tipo, String serie, int correlativo, String cliente, String total) {
	}

}
