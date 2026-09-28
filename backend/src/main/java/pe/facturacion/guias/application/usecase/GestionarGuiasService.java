package pe.facturacion.guias.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.guias.application.port.in.GestionarGuiasUseCase;
import pe.facturacion.guias.application.port.out.GuiaRepositoryPort;
import pe.facturacion.guias.domain.model.Guia;
import pe.facturacion.guias.domain.model.LineaGuia;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public class GestionarGuiasService implements GestionarGuiasUseCase {

	private final GestionarSeriesUseCase series;
	private final GestionarClientesUseCase clientes;
	private final GestionarProductosUseCase productos;
	private final GuiaRepositoryPort guias;
	private final Transacciones transacciones;
	private final Clock reloj;

	public GestionarGuiasService(GestionarSeriesUseCase series, GestionarClientesUseCase clientes,
			GestionarProductosUseCase productos, GuiaRepositoryPort guias, Transacciones transacciones, Clock reloj) {
		this.series = series;
		this.clientes = clientes;
		this.productos = productos;
		this.guias = guias;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public Guia emitir(Solicitud solicitud) {
		return transacciones.ejecutar(() -> persistir(solicitud));
	}

	private Guia persistir(Solicitud solicitud) {
		Cliente cliente = clientes.obtener(solicitud.clienteId());
		List<LineaGuia> lineas = solicitud.items().stream().map(item -> {
			Producto producto = productos.obtener(item.productoId());
			return new LineaGuia(producto.id(), producto.codigo(), producto.descripcion(), producto.unidadMedida(),
					item.cantidad());
		}).toList();
		int correlativo = series.siguienteCorrelativo(TipoComprobante.GUIA_REMISION, solicitud.serie());
		return guias.guardar(new Guia(null, solicitud.serie(), correlativo, LocalDate.now(reloj), cliente.id(),
				cliente.documento(), cliente.nombre(), solicitud.motivoTraslado(), solicitud.modalidad(),
				solicitud.fechaTraslado(), solicitud.pesoTotal(), solicitud.bultos() < 1 ? 1 : solicitud.bultos(),
				solicitud.ubigeoPartida(), solicitud.direccionPartida(), solicitud.ubigeoLlegada(),
				solicitud.direccionLlegada(), solicitud.transportistaDocumento(), solicitud.transportistaNombre(),
				solicitud.placa(), solicitud.licencia(), EstadoSunat.PENDIENTE, null, solicitud.observacion(), lineas));
	}

	@Override
	public Guia obtener(Long id) {
		return guias.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Guía", id));
	}

	@Override
	public Pagina<Guia> buscar(ConsultaPaginada consulta) {
		return guias.buscar(consulta);
	}

}
