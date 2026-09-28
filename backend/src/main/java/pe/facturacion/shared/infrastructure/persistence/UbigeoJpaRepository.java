package pe.facturacion.shared.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UbigeoJpaRepository extends JpaRepository<UbigeoJpaEntity, String> {

	@Query("""
			select u from UbigeoJpaEntity u
			where :q is null
			   or lower(u.codigo) like :q
			   or lower(u.departamento) like :q
			   or lower(u.provincia) like :q
			   or lower(u.distrito) like :q
			order by u.codigo
			""")
	List<UbigeoJpaEntity> buscar(@Param("q") String q);

}
