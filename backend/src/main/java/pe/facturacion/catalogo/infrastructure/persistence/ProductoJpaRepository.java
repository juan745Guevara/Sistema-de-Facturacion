package pe.facturacion.catalogo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {

	@Query("""
			select p from ProductoJpaEntity p
			where (:categoriaId is null or p.categoriaId = :categoriaId)
			  and (:patron is null or lower(p.codigo) like :patron or lower(p.descripcion) like :patron)
			""")
	Page<ProductoJpaEntity> buscar(@Param("patron") String patron, @Param("categoriaId") Long categoriaId,
			Pageable pagina);

	boolean existsByCodigo(String codigo);

	boolean existsByCodigoAndIdNot(String codigo, Long id);

	boolean existsByCategoriaId(Long categoriaId);

}
