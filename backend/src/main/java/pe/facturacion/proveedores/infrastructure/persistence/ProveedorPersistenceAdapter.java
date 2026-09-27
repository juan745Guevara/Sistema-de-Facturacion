package pe.facturacion.proveedores.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.proveedores.application.port.out.ProveedorRepositoryPort;
import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ProveedorPersistenceAdapter implements ProveedorRepositoryPort {

	private final ProveedorJpaRepository repositorio;
	private final ProveedorPersistenceMapper mapper;

	@Override
	public Pagina<Proveedor> buscar(ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("nombre"))), mapper::aDominio);
	}

	@Override
	public Optional<Proveedor> buscarPorId(Long id) {
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
	public Proveedor guardar(Proveedor proveedor) {
		ProveedorJpaEntity entidad = proveedor.id() == null ? new ProveedorJpaEntity()
				: repositorio.findById(proveedor.id())
						.orElseThrow(() -> new RecursoNoEncontradoException("Proveedor", proveedor.id()));
		mapper.copiar(proveedor, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		repositorio.deleteById(id);
	}

}
