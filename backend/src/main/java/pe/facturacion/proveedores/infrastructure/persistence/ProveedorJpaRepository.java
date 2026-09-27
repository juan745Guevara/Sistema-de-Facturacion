package pe.facturacion.proveedores.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

interface ProveedorJpaRepository extends JpaRepository<ProveedorJpaEntity, Long> {

	@Query("""
			select p from ProveedorJpaEntity p
			where :patron is null or lower(p.nombre) like :patron or lower(p.numeroDocumento) like :patron
			""")
	Page<ProveedorJpaEntity> buscar(@Param("patron") String patron, Pageable pagina);

	boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumentoIdentidad tipo, String numero);

	boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(TipoDocumentoIdentidad tipo, String numero, Long id);

}
