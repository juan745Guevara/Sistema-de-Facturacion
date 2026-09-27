package pe.facturacion.catalogo.application.port.in;

import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GestionarProductosUseCase {

	/** Busca por código o descripción; {@code categoriaId} es opcional. */
	Pagina<Producto> buscar(ConsultaPaginada consulta, Long categoriaId);

	Producto obtener(Long id);

	Producto crear(Producto producto);

	Producto actualizar(Long id, Producto producto);

	void eliminar(Long id);

}
