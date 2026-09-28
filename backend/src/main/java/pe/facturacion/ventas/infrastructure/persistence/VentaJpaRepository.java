package pe.facturacion.ventas.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

	Optional<VentaJpaEntity> findByTipoAndSerieAndCorrelativo(TipoComprobante tipo, String serie, int correlativo);

	@Query("""
			select v from VentaJpaEntity v
			where (:tipo is null or v.tipo = :tipo)
			  and (:desde is null or v.fechaEmision >= :desde)
			  and (:hasta is null or v.fechaEmision <= :hasta)
			  and (:patron is null
			       or lower(v.clienteNombre) like :patron
			       or lower(v.clienteNumeroDocumento) like :patron
			       or lower(concat(v.serie, '-', v.correlativo)) like :patron)
			""")
	Page<VentaJpaEntity> buscar(@Param("tipo") TipoComprobante tipo, @Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta, @Param("patron") String patron, Pageable pagina);

}
