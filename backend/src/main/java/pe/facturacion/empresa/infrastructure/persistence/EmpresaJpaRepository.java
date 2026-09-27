package pe.facturacion.empresa.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface EmpresaJpaRepository extends JpaRepository<EmpresaJpaEntity, Short> {
}
