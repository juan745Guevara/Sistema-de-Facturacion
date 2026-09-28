package pe.facturacion.ventas.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.Paginas;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase.Filtro;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.domain.model.Venta;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class VentaPersistenceAdapter implements VentaRepositoryPort {

	private final VentaJpaRepository repositorio;
	private final VentaPersistenceMapper mapper;

	@Override
	public Optional<Venta> buscarPorId(Long id) {
		return repositorio.findById(id).map(this::completa);
	}

	@Override
	public Optional<Venta> buscar(TipoComprobante tipo, String serie, int correlativo) {
		return repositorio.findByTipoAndSerieAndCorrelativo(tipo, serie, correlativo).map(this::completa);
	}

	@Override
	public Pagina<Venta> buscar(Filtro filtro, ConsultaPaginada consulta) {
		return Paginas.desde(repositorio.buscar(filtro.tipo(), filtro.desde(), filtro.hasta(),
				Paginas.patronBusqueda(consulta), Paginas.solicitud(consulta, Sort.by("fechaEmision").descending()
						.and(Sort.by("serie")).and(Sort.by("correlativo").descending()))),
				this::completa);
	}

	/** Fuerza las colecciones en la transacción, una a la vez, para no disparar MultipleBagFetchException. */
	private Venta completa(VentaJpaEntity entidad) {
		entidad.getLineas().size();
		entidad.getCuotas().size();
		return mapper.aDominio(entidad);
	}

	@Override
	@Transactional
	public Venta guardar(Venta venta) {
		VentaJpaEntity entidad = venta.id() == null ? new VentaJpaEntity()
				: repositorio.findById(venta.id()).orElseThrow(() -> new RecursoNoEncontradoException("Venta", venta.id()));
		mapper.copiar(venta, entidad);
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

}
