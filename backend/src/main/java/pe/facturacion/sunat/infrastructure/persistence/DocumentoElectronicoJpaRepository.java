package pe.facturacion.sunat.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

interface DocumentoElectronicoJpaRepository
		extends JpaRepository<DocumentoElectronicoJpaEntity, Long>, JpaSpecificationExecutor<DocumentoElectronicoJpaEntity> {

	Optional<DocumentoElectronicoJpaEntity> findByTipoAndSerieAndCorrelativo(TipoComprobante tipo, String serie,
			int correlativo);

	boolean existsByTipoAndSerieAndCorrelativo(TipoComprobante tipo, String serie, int correlativo);

}
