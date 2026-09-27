package pe.facturacion.catalogo.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface UnidadMedidaJpaRepository extends JpaRepository<UnidadMedidaJpaEntity, String> {

	List<UnidadMedidaJpaEntity> findByActivaTrue(Sort orden);

}
