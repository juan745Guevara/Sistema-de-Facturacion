package pe.facturacion.clientes.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

	@Query("""
			select c from ClienteJpaEntity c
			where :patron is null or lower(c.nombre) like :patron or lower(c.numeroDocumento) like :patron
			""")
	Page<ClienteJpaEntity> buscar(@Param("patron") String patron, Pageable pagina);

	boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumentoIdentidad tipo, String numero);

	boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(TipoDocumentoIdentidad tipo, String numero, Long id);

}
