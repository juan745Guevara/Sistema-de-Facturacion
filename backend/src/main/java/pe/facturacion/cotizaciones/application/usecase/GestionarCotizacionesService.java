package pe.facturacion.cotizaciones.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.cotizaciones.application.port.in.GestionarCotizacionesUseCase;
import pe.facturacion.cotizaciones.application.port.out.CotizacionRepositoryPort;
import pe.facturacion.cotizaciones.domain.model.Cotizacion;
import pe.facturacion.cotizaciones.domain.model.LineaCotizacion;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public class GestionarCotizacionesService implements GestionarCotizacionesUseCase {

	private final GestionarEmpresaUseCase empresas;
	private final GestionarSeriesUseCase series;
	private final GestionarClientesUseCase clientes;
	private final GestionarProductosUseCase productos;
	private final CotizacionRepositoryPort cotizaciones;
	private final Transacciones transacciones;
	private final Clock reloj;

	public GestionarCotizacionesService(GestionarEmpresaUseCase empresas, GestionarSeriesUseCase series,
			GestionarClientesUseCase clientes, GestionarProductosUseCase productos,
			CotizacionRepositoryPort cotizaciones, Transacciones transacciones, Clock reloj) {
		this.empresas = empresas;
		this.series = series;
		this.clientes = clientes;
		this.productos = productos;
		this.cotizaciones = cotizaciones;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public Cotizacion emitir(Solicitud solicitud) {
		return transacciones.ejecutar(() -> persistir(solicitud));
	}

	private Cotizacion persistir(Solicitud solicitud) {
		Cliente cliente = clientes.obtener(solicitud.clienteId());
		BigDecimal tasa = empresas.obtener().porcentajeIgv().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
		List<LineaCotizacion> lineas = new ArrayList<>();
		BigDecimal gravadas = BigDecimal.ZERO;
		BigDecimal igv = BigDecimal.ZERO;
		for (Item item : solicitud.items()) {
			Producto producto = productos.obtener(item.productoId());
			BigDecimal precio = item.precioUnitario() != null ? item.precioUnitario() : producto.precioVenta();
			BigDecimal valor = precio.divide(tasa.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP)
					.multiply(item.cantidad()).setScale(2, RoundingMode.HALF_UP);
			BigDecimal impuesto = valor.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
			lineas.add(new LineaCotizacion(producto.id(), producto.codigo(), producto.descripcion(),
					producto.unidadMedida(), item.cantidad(), precio, valor, impuesto));
			gravadas = gravadas.add(valor);
			igv = igv.add(impuesto);
		}
		int correlativo = series.siguienteCorrelativo(TipoComprobante.COTIZACION, solicitud.serie());
		return cotizaciones.guardar(new Cotizacion(null, solicitud.serie(), correlativo, LocalDate.now(reloj),
				cliente.id(), cliente.documento(), cliente.nombre(), Moneda.PEN, lineas, gravadas, igv,
				gravadas.add(igv), solicitud.observacion()));
	}

	@Override
	public Cotizacion obtener(Long id) {
		return cotizaciones.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Cotización", id));
	}

	@Override
	public Pagina<Cotizacion> buscar(ConsultaPaginada consulta) {
		return cotizaciones.buscar(consulta);
	}

}
