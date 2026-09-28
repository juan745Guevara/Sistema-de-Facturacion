package pe.facturacion.notas.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class NotaPersistenceAdapter implements NotaRepositoryPort {

	private final NotaJpaRepository repositorio;
	private final NotaPersistenceMapper mapper;

	@Override
	public Optional<Nota> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Optional<Nota> buscar(TipoComprobante tipo, String serie, int correlativo) {
		return repositorio.findByTipoAndSerieAndCorrelativo(tipo, serie, correlativo).map(this::completa);
	}

	@Override
	public Pagina<Nota> buscar(TipoComprobante tipo, ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(tipo, Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("fechaEmision").descending().and(Sort.by("serie"))
						.and(Sort.by("correlativo").descending()))),
				this::completa);
	}

	@Override
	public BigDecimal totalAcreditado(TipoComprobante tipoReferencia, String serieReferencia, int correlativoReferencia) {
		BigDecimal total = repositorio.totalAcreditado(TipoComprobante.NOTA_CREDITO, tipoReferencia, serieReferencia,
				correlativoReferencia, java.util.List.of(EstadoSunat.RECHAZADO, EstadoSunat.ANULADO));
		return total == null ? BigDecimal.ZERO : total;
	}

	@Override
	@Transactional
	public Nota guardar(Nota nota) {
		NotaJpaEntity entidad = nota.id() == null ? new NotaJpaEntity()
				: repositorio.findById(nota.id()).orElseThrow(() -> new RecursoNoEncontradoException("Nota", nota.id()));
		mapper.copiar(nota, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	private Nota completa(NotaJpaEntity entidad) {
		entidad.getLineas().size();
		return mapper.aDominio(entidad);
	}

}
