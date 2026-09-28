package pe.facturacion.cotizaciones.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.cotizaciones.application.port.out.CotizacionRepositoryPort;
import pe.facturacion.cotizaciones.domain.model.Cotizacion;
import pe.facturacion.cotizaciones.domain.model.LineaCotizacion;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CotizacionPersistenceAdapter implements CotizacionRepositoryPort {

	private final CotizacionJpaRepository repositorio;

	@Override
	public Optional<Cotizacion> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Pagina<Cotizacion> buscar(ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("fechaEmision").descending())), this::completa);
	}

	@Override
	@Transactional
	public Cotizacion guardar(Cotizacion cotizacion) {
		CotizacionJpaEntity entidad = cotizacion.id() == null ? new CotizacionJpaEntity()
				: repositorio.findById(cotizacion.id())
						.orElseThrow(() -> new RecursoNoEncontradoException("Cotización", cotizacion.id()));
		copiar(cotizacion, entidad);
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	private Cotizacion completa(CotizacionJpaEntity entidad) {
		entidad.getLineas().size();
		return aDominio(entidad);
	}

	private Cotizacion aDominio(CotizacionJpaEntity e) {
		List<LineaCotizacion> lineas = e.getLineas().stream()
				.map(l -> new LineaCotizacion(l.getProductoId(), l.getCodigo(), l.getDescripcion(), l.getUnidadMedida(),
						l.getCantidad(), l.getPrecioUnitario(), l.getValorVenta(), l.getImpuesto()))
				.toList();
		return new Cotizacion(e.getId(), e.getSerie(), e.getCorrelativo(), e.getFechaEmision(), e.getClienteId(),
				new DocumentoIdentidad(e.getClienteTipoDocumento(), e.getClienteNumeroDocumento()), e.getClienteNombre(),
				e.getMoneda(), lineas, e.getGravadas(), e.getIgv(), e.getTotal(), e.getObservacion());
	}

	private static void copiar(Cotizacion c, CotizacionJpaEntity e) {
		e.setSerie(c.serie());
		e.setCorrelativo(c.correlativo());
		e.setFechaEmision(c.fechaEmision());
		e.setClienteId(c.clienteId());
		e.setClienteTipoDocumento(c.clienteDocumento().tipo());
		e.setClienteNumeroDocumento(c.clienteDocumento().numero());
		e.setClienteNombre(c.clienteNombre());
		e.setMoneda(c.moneda());
		e.setGravadas(c.gravadas());
		e.setIgv(c.igv());
		e.setTotal(c.total());
		e.setObservacion(c.observacion());
		e.getLineas().clear();
		List<LineaCotizacionJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaCotizacion linea : c.lineas()) {
			LineaCotizacionJpaEntity fila = new LineaCotizacionJpaEntity();
			fila.setCotizacion(e);
			fila.setProductoId(linea.productoId());
			fila.setCodigo(linea.codigo());
			fila.setDescripcion(linea.descripcion());
			fila.setUnidadMedida(linea.unidadMedida());
			fila.setCantidad(linea.cantidad());
			fila.setPrecioUnitario(linea.precioUnitario());
			fila.setValorVenta(linea.valorVenta());
			fila.setImpuesto(linea.impuesto());
			fila.setOrden(orden++);
			lineas.add(fila);
		}
		e.getLineas().addAll(lineas);
	}

}
