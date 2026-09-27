package pe.facturacion.catalogo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {

	boolean existsByNombre(String nombre);

	boolean existsByNombreAndIdNot(String nombre, Long id);

}
