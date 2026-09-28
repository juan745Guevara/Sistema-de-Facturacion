package pe.facturacion.compras.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.compras.application.port.out.CompraRepositoryPort;
import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.compras.domain.model.LineaCompra;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.Paginas;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CompraPersistenceAdapter implements CompraRepositoryPort {

	private final CompraJpaRepository repositorio;

	@Override
	public Optional<Compra> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Pagina<Compra> buscar(ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(Paginas.patronBusqueda(consulta),
				Paginas.solicitud(consulta, Sort.by("fechaEmision").descending())), this::completa);
	}

	@Override
	public boolean existe(String tipo, String serie, String correlativo) {
		return repositorio.existsByTipoAndSerieAndCorrelativo(TipoComprobante.desdeCodigo(tipo),
				serie.trim().toUpperCase(), correlativo.trim());
	}

	@Override
	@Transactional
	public Compra guardar(Compra compra) {
		CompraJpaEntity entidad = compra.id() == null ? new CompraJpaEntity()
				: repositorio.findById(compra.id())
						.orElseThrow(() -> new RecursoNoEncontradoException("Compra", compra.id()));
		copiar(compra, entidad);
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	private Compra completa(CompraJpaEntity entidad) {
		entidad.getLineas().size();
		return aDominio(entidad);
	}

	private Compra aDominio(CompraJpaEntity e) {
		List<LineaCompra> lineas = e.getLineas().stream()
				.map(l -> new LineaCompra(l.getProductoId(), l.getCodigo(), l.getDescripcion(), l.getUnidadMedida(),
						l.getCantidad(), l.getPrecioUnitario(), l.getValorVenta(), l.getImpuesto()))
				.toList();
		return new Compra(e.getId(), e.getTipo(), e.getSerie(), e.getCorrelativo(), e.getFechaEmision(),
				e.getProveedorId(), new DocumentoIdentidad(e.getProveedorTipoDocumento(), e.getProveedorNumeroDocumento()),
				e.getProveedorNombre(), e.getMoneda(), lineas, e.getGravadas(), e.getIgv(), e.getTotal(), e.isAnulada(),
				e.getObservacion());
	}

	private static void copiar(Compra compra, CompraJpaEntity e) {
		e.setTipo(compra.tipo());
		e.setSerie(compra.serie());
		e.setCorrelativo(compra.correlativo());
		e.setFechaEmision(compra.fechaEmision());
		e.setProveedorId(compra.proveedorId());
		e.setProveedorTipoDocumento(compra.proveedorDocumento().tipo());
		e.setProveedorNumeroDocumento(compra.proveedorDocumento().numero());
		e.setProveedorNombre(compra.proveedorNombre());
		e.setMoneda(compra.moneda());
		e.setGravadas(compra.gravadas());
		e.setIgv(compra.igv());
		e.setTotal(compra.total());
		e.setAnulada(compra.anulada());
		e.setObservacion(compra.observacion());
		e.getLineas().clear();
		List<LineaCompraJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaCompra linea : compra.lineas()) {
			LineaCompraJpaEntity fila = new LineaCompraJpaEntity();
			fila.setCompra(e);
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
