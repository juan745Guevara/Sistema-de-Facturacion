package pe.facturacion.guias.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface GuiaJpaRepository extends JpaRepository<GuiaJpaEntity, Long> {

	@Query("""
			select g from GuiaJpaEntity g
			where (:q is null or lower(g.clienteNombre) like :q escape '\\' or lower(g.serie) like :q escape '\\')
			""")
	Page<GuiaJpaEntity> buscar(@Param("q") String q, Pageable pageable);

}
