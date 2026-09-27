package pe.facturacion.proveedores.application.port.in;

import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GestionarProveedoresUseCase {

	/** Busca por nombre o número de documento. */
	Pagina<Proveedor> buscar(ConsultaPaginada consulta);

	Proveedor obtener(Long id);

	Proveedor crear(Proveedor proveedor);

	Proveedor actualizar(Long id, Proveedor proveedor);

	void eliminar(Long id);

}
