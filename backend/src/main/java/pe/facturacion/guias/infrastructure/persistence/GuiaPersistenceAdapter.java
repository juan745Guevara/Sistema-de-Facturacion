package pe.facturacion.guias.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.guias.application.port.out.GuiaRepositoryPort;
import pe.facturacion.guias.domain.model.Guia;
import pe.facturacion.guias.domain.model.LineaGuia;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GuiaPersistenceAdapter implements GuiaRepositoryPort {

	private final GuiaJpaRepository repositorio;

	@Override
	public Optional<Guia> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Pagina<Guia> buscar(ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("fechaEmision").descending())), this::completa);
	}

	@Override
	@Transactional
	public Guia guardar(Guia guia) {
		GuiaJpaEntity entidad = guia.id() == null ? new GuiaJpaEntity()
				: repositorio.findById(guia.id()).orElseThrow(() -> new RecursoNoEncontradoException("Guía", guia.id()));
		copiar(guia, entidad);
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	private Guia completa(GuiaJpaEntity entidad) {
		entidad.getLineas().size();
		return aDominio(entidad);
	}

	private Guia aDominio(GuiaJpaEntity e) {
		List<LineaGuia> lineas = e.getLineas().stream()
				.map(l -> new LineaGuia(l.getProductoId(), l.getCodigo(), l.getDescripcion(), l.getUnidadMedida(),
						l.getCantidad()))
				.toList();
		return new Guia(e.getId(), e.getSerie(), e.getCorrelativo(), e.getFechaEmision(), e.getClienteId(),
				new DocumentoIdentidad(e.getClienteTipoDocumento(), e.getClienteNumeroDocumento()), e.getClienteNombre(),
				e.getMotivoTraslado(), e.getModalidad(), e.getFechaTraslado(), e.getPesoTotal(), e.getBultos(),
				e.getUbigeoPartida(), e.getDireccionPartida(), e.getUbigeoLlegada(), e.getDireccionLlegada(),
				e.getTransportistaDocumento(), e.getTransportistaNombre(), e.getPlaca(), e.getLicencia(),
				e.getEstadoSunat(), e.getTicket(), e.getObservacion(), lineas);
	}

	private static void copiar(Guia g, GuiaJpaEntity e) {
		e.setSerie(g.serie());
		e.setCorrelativo(g.correlativo());
		e.setFechaEmision(g.fechaEmision());
		e.setClienteId(g.clienteId());
		e.setClienteTipoDocumento(g.destinatario().tipo());
		e.setClienteNumeroDocumento(g.destinatario().numero());
		e.setClienteNombre(g.destinatarioNombre());
		e.setMotivoTraslado(g.motivoTraslado());
		e.setModalidad(g.modalidad());
		e.setFechaTraslado(g.fechaTraslado());
		e.setPesoTotal(g.pesoTotal());
		e.setBultos(g.bultos());
		e.setUbigeoPartida(g.ubigeoPartida());
		e.setDireccionPartida(g.direccionPartida());
		e.setUbigeoLlegada(g.ubigeoLlegada());
		e.setDireccionLlegada(g.direccionLlegada());
		e.setTransportistaDocumento(g.transportistaDocumento());
		e.setTransportistaNombre(g.transportistaNombre());
		e.setPlaca(g.placa());
		e.setLicencia(g.licencia());
		e.setEstadoSunat(g.estadoSunat());
		e.setTicket(g.ticket());
		e.setObservacion(g.observacion());
		e.getLineas().clear();
		List<LineaGuiaJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaGuia linea : g.lineas()) {
			LineaGuiaJpaEntity fila = new LineaGuiaJpaEntity();
			fila.setGuia(e);
			fila.setProductoId(linea.productoId());
			fila.setCodigo(linea.codigo());
			fila.setDescripcion(linea.descripcion());
			fila.setUnidadMedida(linea.unidadMedida());
			fila.setCantidad(linea.cantidad());
			fila.setOrden(orden++);
			lineas.add(fila);
		}
		e.getLineas().addAll(lineas);
	}

}
