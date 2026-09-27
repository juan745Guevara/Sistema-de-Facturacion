package pe.facturacion.clientes.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.clientes.application.port.out.ClienteRepositoryPort;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ClientePersistenceAdapter implements ClienteRepositoryPort {

	private final ClienteJpaRepository repositorio;
	private final ClientePersistenceMapper mapper;

	@Override
	public Pagina<Cliente> buscar(ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("nombre"))), mapper::aDominio);
	}

	@Override
	public Optional<Cliente> buscarPorId(Long id) {
		return repositorio.findById(id).map(mapper::aDominio);
	}

	@Override
	public boolean existeDocumento(DocumentoIdentidad documento, Long excluirId) {
		return excluirId == null
				? repositorio.existsByTipoDocumentoAndNumeroDocumento(documento.tipo(), documento.numero())
				: repositorio.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(documento.tipo(), documento.numero(),
						excluirId);
	}

	@Override
	@Transactional
	public Cliente guardar(Cliente cliente) {
		ClienteJpaEntity entidad = cliente.id() == null ? new ClienteJpaEntity()
				: repositorio.findById(cliente.id())
						.orElseThrow(() -> new RecursoNoEncontradoException("Cliente", cliente.id()));
		mapper.copiar(cliente, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		repositorio.deleteById(id);
	}

}
