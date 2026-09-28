package pe.facturacion.compras.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

interface CompraJpaRepository extends JpaRepository<CompraJpaEntity, Long> {

	boolean existsByTipoAndSerieAndCorrelativo(TipoComprobante tipo, String serie, String correlativo);

	@Query("""
			select c from CompraJpaEntity c
			where (:q is null or lower(c.proveedorNombre) like :q escape '\\'
			       or lower(c.serie) like :q escape '\\'
			       or lower(c.correlativo) like :q escape '\\')
			""")
	Page<CompraJpaEntity> buscar(@Param("q") String q, Pageable pageable);

}
