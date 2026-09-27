package pe.facturacion.catalogo.application.port.out;

import java.util.List;
import java.util.Optional;

import pe.facturacion.catalogo.domain.model.UnidadMedida;

public interface UnidadMedidaRepositoryPort {

	List<UnidadMedida> listar(boolean soloActivas);

	Optional<UnidadMedida> buscarPorCodigo(String codigo);

	UnidadMedida guardar(UnidadMedida unidad);

}
