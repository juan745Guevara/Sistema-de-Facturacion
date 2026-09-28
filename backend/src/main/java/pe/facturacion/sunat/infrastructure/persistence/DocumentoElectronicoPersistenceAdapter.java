package pe.facturacion.sunat.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.Paginas;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class DocumentoElectronicoPersistenceAdapter implements DocumentoElectronicoRepositoryPort {

	private final DocumentoElectronicoJpaRepository repositorio;

	@Override
	public boolean existe(TipoComprobante tipo, String serie, int correlativo) {
		return repositorio.existsByTipoAndSerieAndCorrelativo(tipo, serie, correlativo);
	}

	@Override
	public Optional<DocumentoElectronico> buscar(TipoComprobante tipo, String serie, int correlativo) {
		return repositorio.findByTipoAndSerieAndCorrelativo(tipo, serie, correlativo).map(this::aDominio);
	}

	@Override
	public Optional<ComprobanteElectronico> datos(TipoComprobante tipo, String serie, int correlativo) {
		return repositorio.findByTipoAndSerieAndCorrelativo(tipo, serie, correlativo)
				.map(DocumentoElectronicoJpaEntity::getDatos);
	}

	@Override
	public Pagina<DocumentoElectronico> buscar(FiltroDocumentos filtro, ConsultaPaginada consulta) {
		Specification<DocumentoElectronicoJpaEntity> spec = (root, query, cb) -> cb.conjunction();
		if (filtro.tipo() != null) {
			spec = spec.and((root, q, cb) -> cb.equal(root.get("tipo"), filtro.tipo()));
		}
		if (filtro.estado() != null) {
			spec = spec.and((root, q, cb) -> cb.equal(root.get("estado"), filtro.estado()));
		}
		if (filtro.desde() != null) {
			spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("fechaEmision"), filtro.desde()));
		}
		if (filtro.hasta() != null) {
			spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("fechaEmision"), filtro.hasta()));
		}
		return Paginas.desde(repositorio.findAll(spec,
				Paginas.solicitud(consulta, Sort.by("fechaEmision").descending().and(Sort.by("serie"))
						.and(Sort.by("correlativo").descending()))),
				this::aDominio);
	}

	@Override
	public List<DocumentoElectronico> listar(TipoComprobante tipo, LocalDate fecha, EstadoSunat estado) {
		return repositorio.findByTipoAndFechaEmisionAndEstado(tipo, fecha, estado).stream()
				.map(this::aDominio)
				.toList();
	}

	@Override
	@Transactional
	public DocumentoElectronico registrar(DocumentoElectronico documento, ComprobanteElectronico datos) {
		DocumentoElectronicoJpaEntity entidad = new DocumentoElectronicoJpaEntity();
		copiar(documento, entidad);
		entidad.setDatos(datos);
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	@Transactional
	public DocumentoElectronico actualizar(DocumentoElectronico documento) {
		DocumentoElectronicoJpaEntity entidad = repositorio
				.findByTipoAndSerieAndCorrelativo(documento.tipo(), documento.serie(), documento.correlativo())
				.orElseThrow(() -> new RecursoNoEncontradoException("Documento electrónico", documento.numero()));
		copiar(documento, entidad);
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	private DocumentoElectronico aDominio(DocumentoElectronicoJpaEntity entidad) {
		List<String> observaciones = entidad.getObservaciones() == null || entidad.getObservaciones().isBlank()
				? List.of()
				: Arrays.asList(entidad.getObservaciones().split("\n"));
		return new DocumentoElectronico(entidad.getId(), entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(),
				entidad.getFechaEmision(), entidad.getEstado(), entidad.getCodigoRespuesta(), entidad.getMensaje(),
				observaciones, entidad.getHash(), entidad.getXmlFirmado(), entidad.getCdr(), entidad.getIntentos(),
				entidad.getUltimoEnvio());
	}

	private static void copiar(DocumentoElectronico documento, DocumentoElectronicoJpaEntity entidad) {
		entidad.setTipo(documento.tipo());
		entidad.setSerie(documento.serie());
		entidad.setCorrelativo(documento.correlativo());
		entidad.setFechaEmision(documento.fechaEmision());
		entidad.setEstado(documento.estado());
		entidad.setCodigoRespuesta(documento.codigoRespuesta());
		entidad.setMensaje(documento.mensaje());
		entidad.setObservaciones(documento.observaciones().isEmpty() ? null : String.join("\n", documento.observaciones()));
		entidad.setHash(documento.hash());
		entidad.setXmlFirmado(documento.xmlFirmado());
		entidad.setCdr(documento.cdr());
		entidad.setIntentos(documento.intentos());
		entidad.setUltimoEnvio(documento.ultimoEnvio());
	}

}
