package pe.facturacion.clientes.application.port.in;

import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public interface GestionarClientesUseCase {

	/** Busca por nombre o número de documento. */
	Pagina<Cliente> buscar(ConsultaPaginada consulta);

	Cliente obtener(Long id);

	Cliente crear(Cliente cliente);

	Cliente actualizar(Long id, Cliente cliente);

	void eliminar(Long id);

}
