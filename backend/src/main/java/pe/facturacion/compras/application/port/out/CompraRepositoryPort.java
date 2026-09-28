package pe.facturacion.compras.application.port.out;

import java.util.Optional;

import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface CompraRepositoryPort {

	Optional<Compra> buscarPorId(Long id);

	Pagina<Compra> buscar(ConsultaPaginada consulta);

	boolean existe(String tipo, String serie, String correlativo);

	Compra guardar(Compra compra);

}
