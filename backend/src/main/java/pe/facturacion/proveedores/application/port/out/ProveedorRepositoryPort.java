package pe.facturacion.proveedores.application.port.out;

import java.util.Optional;

import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

public interface ProveedorRepositoryPort {

	Pagina<Proveedor> buscar(ConsultaPaginada consulta);

	Optional<Proveedor> buscarPorId(Long id);

	boolean existeDocumento(DocumentoIdentidad documento, Long excluirId);

	Proveedor guardar(Proveedor proveedor);

	void eliminar(Long id);

}
