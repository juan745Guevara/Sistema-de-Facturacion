package pe.facturacion.empresa.application.port.out;

import java.util.List;
import java.util.Optional;

import pe.facturacion.empresa.domain.model.Serie;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface SerieRepositoryPort {

	List<Serie> listar(TipoComprobante tipo);

	Optional<Serie> buscarPorId(Long id);

	boolean existe(TipoComprobante tipo, String serie);

	/** Lee la serie con bloqueo de escritura hasta que termine la transacción actual. */
	Optional<Serie> bloquear(TipoComprobante tipo, String serie);

	Serie guardar(Serie serie);

}
