package pe.facturacion.ventas.application.port.out;

import java.util.Optional;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase.Filtro;
import pe.facturacion.ventas.domain.model.Venta;

public interface VentaRepositoryPort {

	Optional<Venta> buscarPorId(Long id);

	Optional<Venta> buscar(TipoComprobante tipo, String serie, int correlativo);

	Pagina<Venta> buscar(Filtro filtro, ConsultaPaginada consulta);

	Venta guardar(Venta venta);

}
