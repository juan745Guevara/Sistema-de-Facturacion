package pe.facturacion.seguridad.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.model.Usuario;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

@Component
@RequiredArgsConstructor
class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

	private final UsuarioJpaRepository repositorio;
	private final UsuarioPersistenceMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorUsername(String username) {
		return repositorio.findByUsername(username).map(mapper::aDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existeAlguno() {
		return repositorio.count() > 0;
	}

	@Override
	@Transactional
	public Usuario guardar(Usuario usuario) {
		UsuarioJpaEntity entidad = usuario.id() == null
				? new UsuarioJpaEntity()
				: repositorio.findById(usuario.id())
						.orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuario.id()));
		mapper.copiar(usuario, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

}
