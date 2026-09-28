package pe.facturacion.guias.application.port.out;

import java.util.Optional;

import pe.facturacion.guias.domain.model.Guia;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GuiaRepositoryPort {

	Optional<Guia> buscarPorId(Long id);

	Pagina<Guia> buscar(ConsultaPaginada consulta);

	Guia guardar(Guia guia);

}
