package pe.facturacion.notas.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

interface NotaJpaRepository extends JpaRepository<NotaJpaEntity, Long> {

	Optional<NotaJpaEntity> findByTipoAndSerieAndCorrelativo(TipoComprobante tipo, String serie, int correlativo);

	@Query("""
			select n from NotaJpaEntity n
			where (:tipo is null or n.tipo = :tipo)
			  and (:q is null or lower(n.serie) like :q escape '\\'
			       or lower(n.clienteNombre) like :q escape '\\'
			       or lower(n.clienteNumeroDocumento) like :q escape '\\'
			       or concat(n.serie, '-', n.correlativo) like :q escape '\\')
			""")
	Page<NotaJpaEntity> buscar(@Param("tipo") TipoComprobante tipo, @Param("q") String q, Pageable pageable);

	@Query("""
			select coalesce(sum(n.total), 0) from NotaJpaEntity n
			where n.tipo = :tipoNota
			  and n.tipoReferencia = :tipo
			  and n.serieReferencia = :serie
			  and n.correlativoReferencia = :correlativo
			  and n.estadoSunat not in :excluidos
			""")
	BigDecimal totalAcreditado(@Param("tipoNota") TipoComprobante tipoNota, @Param("tipo") TipoComprobante tipo,
			@Param("serie") String serie, @Param("correlativo") int correlativo,
			@Param("excluidos") java.util.Collection<pe.facturacion.shared.domain.model.sunat.EstadoSunat> excluidos);

}
