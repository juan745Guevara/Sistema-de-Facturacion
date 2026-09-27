package pe.facturacion.clientes.application.usecase;

import java.util.Objects;

import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.application.port.out.ClienteRepositoryPort;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarClientesService implements GestionarClientesUseCase {

	private final ClienteRepositoryPort clientes;

	public GestionarClientesService(ClienteRepositoryPort clientes) {
		this.clientes = Objects.requireNonNull(clientes);
	}

	@Override
	public Pagina<Cliente> buscar(ConsultaPaginada consulta) {
		return clientes.buscar(consulta);
	}

	@Override
	public Cliente obtener(Long id) {
		return clientes.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
	}

	@Override
	public Cliente crear(Cliente cliente) {
		Cliente nuevo = cliente.conId(null);
		exigirDocumentoLibre(nuevo);
		return clientes.guardar(nuevo);
	}

	@Override
	public Cliente actualizar(Long id, Cliente cliente) {
		obtener(id);
		Cliente cambiado = cliente.conId(id);
		exigirDocumentoLibre(cambiado);
		return clientes.guardar(cambiado);
	}

	@Override
	public void eliminar(Long id) {
		obtener(id);
		clientes.eliminar(id);
	}

	private void exigirDocumentoLibre(Cliente cliente) {
		if (clientes.existeDocumento(cliente.documento(), cliente.id())) {
			throw DominioException.conflicto("cliente-duplicado", "Ya existe un cliente con el %s %s"
					.formatted(cliente.documento().tipo().descripcion(), cliente.documento().numero()));
		}
	}

}
