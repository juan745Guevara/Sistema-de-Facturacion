package pe.facturacion.sunat.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.infrastructure.persistence.Paginas;
import pe.facturacion.sunat.application.port.out.LoteRepositoryPort;
import pe.facturacion.sunat.domain.model.LoteSunat;
import pe.facturacion.sunat.domain.model.TipoLote;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LotePersistenceAdapter implements LoteRepositoryPort {

	private final LoteJpaRepository repositorio;
	private final LotePersistenceMapper mapper;

	@Override
	public Optional<LoteSunat> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Pagina<LoteSunat> buscar(TipoLote tipo, ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(tipo,
				Paginas.solicitud(consulta, Sort.by("fechaGeneracion").descending().and(Sort.by("correlativo").descending()))),
				this::completa);
	}

	@Override
	@Transactional
	public int siguienteCorrelativo(TipoLote tipo, LocalDate fechaGeneracion) {
		return repositorio.maximoCorrelativo(tipo, fechaGeneracion).orElse(0) + 1;
	}

	@Override
	@Transactional
	public LoteSunat guardar(LoteSunat lote) {
		LoteJpaEntity entidad = lote.id() == null ? new LoteJpaEntity()
				: repositorio.findById(lote.id()).orElseThrow(() -> new RecursoNoEncontradoException("Lote SUNAT", lote.id()));
		mapper.copiar(lote, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	private LoteSunat completa(LoteJpaEntity entidad) {
		entidad.getLineas().size();
		return mapper.aDominio(entidad);
	}

}
