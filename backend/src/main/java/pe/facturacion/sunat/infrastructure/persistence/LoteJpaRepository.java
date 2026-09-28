package pe.facturacion.sunat.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import pe.facturacion.sunat.domain.model.TipoLote;

interface LoteJpaRepository extends JpaRepository<LoteJpaEntity, Long> {

	@Query("select l from LoteJpaEntity l where (:tipo is null or l.tipo = :tipo)")
	Page<LoteJpaEntity> buscar(@Param("tipo") TipoLote tipo, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select max(l.correlativo) from LoteJpaEntity l where l.tipo = :tipo and l.fechaGeneracion = :fecha")
	Optional<Integer> maximoCorrelativo(@Param("tipo") TipoLote tipo, @Param("fecha") LocalDate fecha);

}
