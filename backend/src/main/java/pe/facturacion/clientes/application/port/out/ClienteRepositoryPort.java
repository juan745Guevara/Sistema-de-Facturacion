package pe.facturacion.clientes.application.port.out;

import java.util.Optional;

import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

public interface ClienteRepositoryPort {

	Pagina<Cliente> buscar(ConsultaPaginada consulta);

	Optional<Cliente> buscarPorId(Long id);

	boolean existeDocumento(DocumentoIdentidad documento, Long excluirId);

	Cliente guardar(Cliente cliente);

	void eliminar(Long id);

}
