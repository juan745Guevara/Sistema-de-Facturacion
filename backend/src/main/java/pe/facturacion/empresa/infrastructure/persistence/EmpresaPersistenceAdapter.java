package pe.facturacion.empresa.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.empresa.application.port.out.EmpresaRepositoryPort;
import pe.facturacion.empresa.domain.model.Empresa;

@Component
@RequiredArgsConstructor
class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

	private final EmpresaJpaRepository repositorio;
	private final EmpresaPersistenceMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public Optional<Empresa> obtener() {
		return repositorio.findById(EmpresaJpaEntity.ID_UNICO).map(mapper::aDominio);
	}

	@Override
	@Transactional
	public Empresa guardar(Empresa empresa) {
		EmpresaJpaEntity entidad = repositorio.findById(EmpresaJpaEntity.ID_UNICO).orElseGet(() -> {
			EmpresaJpaEntity nueva = new EmpresaJpaEntity();
			nueva.setId(EmpresaJpaEntity.ID_UNICO);
			return nueva;
		});
		mapper.copiar(empresa, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

}
