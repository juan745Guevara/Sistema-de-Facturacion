package pe.facturacion.cotizaciones.application.port.out;

import java.util.Optional;

import pe.facturacion.cotizaciones.domain.model.Cotizacion;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface CotizacionRepositoryPort {

	Optional<Cotizacion> buscarPorId(Long id);

	Pagina<Cotizacion> buscar(ConsultaPaginada consulta);

	Cotizacion guardar(Cotizacion cotizacion);

}
