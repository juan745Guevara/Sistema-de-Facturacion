package pe.facturacion.proveedores.application.usecase;

import java.util.Objects;

import pe.facturacion.proveedores.application.port.in.GestionarProveedoresUseCase;
import pe.facturacion.proveedores.application.port.out.ProveedorRepositoryPort;
import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarProveedoresService implements GestionarProveedoresUseCase {

	private final ProveedorRepositoryPort proveedores;

	public GestionarProveedoresService(ProveedorRepositoryPort proveedores) {
		this.proveedores = Objects.requireNonNull(proveedores);
	}

	@Override
	public Pagina<Proveedor> buscar(ConsultaPaginada consulta) {
		return proveedores.buscar(consulta);
	}

	@Override
	public Proveedor obtener(Long id) {
		return proveedores.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor", id));
	}

	@Override
	public Proveedor crear(Proveedor proveedor) {
		Proveedor nuevo = proveedor.conId(null);
		exigirDocumentoLibre(nuevo);
		return proveedores.guardar(nuevo);
	}

	@Override
	public Proveedor actualizar(Long id, Proveedor proveedor) {
		obtener(id);
		Proveedor cambiado = proveedor.conId(id);
		exigirDocumentoLibre(cambiado);
		return proveedores.guardar(cambiado);
	}

	@Override
	public void eliminar(Long id) {
		obtener(id);
		proveedores.eliminar(id);
	}

	private void exigirDocumentoLibre(Proveedor proveedor) {
		if (proveedores.existeDocumento(proveedor.documento(), proveedor.id())) {
			throw DominioException.conflicto("proveedor-duplicado", "Ya existe un proveedor con el %s %s"
					.formatted(proveedor.documento().tipo().descripcion(), proveedor.documento().numero()));
		}
	}

}
