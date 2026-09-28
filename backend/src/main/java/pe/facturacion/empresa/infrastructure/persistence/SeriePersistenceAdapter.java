package pe.facturacion.empresa.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.empresa.application.port.out.SerieRepositoryPort;
import pe.facturacion.empresa.domain.model.Serie;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SeriePersistenceAdapter implements SerieRepositoryPort {

	private static final Sort ORDEN = Sort.by("tipo", "serie");

	private final SerieJpaRepository repositorio;

	@Override
	public List<Serie> listar(TipoComprobante tipo) {
		List<SerieJpaEntity> encontradas = tipo == null ? repositorio.findAll(ORDEN) : repositorio.findByTipo(tipo, ORDEN);
		return encontradas.stream().map(SeriePersistenceAdapter::aDominio).toList();
	}

	@Override
	public Optional<Serie> buscarPorId(Long id) {
		return repositorio.findById(id).map(SeriePersistenceAdapter::aDominio);
	}

	@Override
	public boolean existe(TipoComprobante tipo, String serie) {
		return repositorio.existsByTipoAndSerie(tipo, serie);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<Serie> bloquear(TipoComprobante tipo, String serie) {
		return repositorio.bloquear(tipo, serie).map(SeriePersistenceAdapter::aDominio);
	}

	@Override
	@Transactional
	public Serie guardar(Serie serie) {
		SerieJpaEntity entidad = serie.id() == null ? new SerieJpaEntity()
				: repositorio.findById(serie.id()).orElseThrow(() -> new RecursoNoEncontradoException("Serie", serie.id()));
		entidad.setTipo(serie.tipo());
		entidad.setSerie(serie.serie());
		entidad.setCorrelativo(serie.correlativo());
		entidad.setActiva(serie.activa());
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	private static Serie aDominio(SerieJpaEntity entidad) {
		return new Serie(entidad.getId(), entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(),
				entidad.isActiva());
	}

}
