package pe.facturacion.cotizaciones.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CotizacionJpaRepository extends JpaRepository<CotizacionJpaEntity, Long> {

	@Query("""
			select c from CotizacionJpaEntity c
			where (:q is null or lower(c.clienteNombre) like :q escape '\\'
			       or lower(c.serie) like :q escape '\\')
			""")
	Page<CotizacionJpaEntity> buscar(@Param("q") String q, Pageable pageable);

}
