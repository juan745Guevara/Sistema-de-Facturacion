package pe.facturacion.empresa.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

interface SerieJpaRepository extends JpaRepository<SerieJpaEntity, Long> {

	List<SerieJpaEntity> findByTipo(TipoComprobante tipo, Sort orden);

	boolean existsByTipoAndSerie(TipoComprobante tipo, String serie);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from SerieJpaEntity s where s.tipo = :tipo and s.serie = :serie")
	Optional<SerieJpaEntity> bloquear(TipoComprobante tipo, String serie);

}
