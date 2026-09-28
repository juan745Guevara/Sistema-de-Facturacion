package pe.facturacion.ventas.application.port.in;

import java.time.LocalDate;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.ventas.domain.model.Venta;

public interface ConsultarVentasUseCase {

	Venta obtener(Long id);

	Venta obtener(TipoComprobante tipo, String serie, int correlativo);

	Pagina<Venta> buscar(Filtro filtro, ConsultaPaginada consulta);

	record Filtro(TipoComprobante tipo, LocalDate desde, LocalDate hasta) {

		public static Filtro todos() {
			return new Filtro(null, null, null);
		}
	}

}
