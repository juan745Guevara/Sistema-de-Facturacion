package pe.facturacion.ventas.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.domain.model.Empresa;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.domain.model.Cuota;
import pe.facturacion.ventas.domain.model.FormaPago;
import pe.facturacion.ventas.domain.model.LineaVenta;
import pe.facturacion.ventas.domain.model.Venta;

public class EmitirVentaService implements EmitirVentaUseCase {

	private final GestionarEmpresaUseCase empresas;
	private final GestionarSeriesUseCase series;
	private final GestionarClientesUseCase clientes;
	private final GestionarProductosUseCase productos;
	private final AjustarStockUseCase stock;
	private final VentaRepositoryPort ventas;
	private final RegistrarDocumentoUseCase registrarSunat;
	private final EnviarDocumentoUseCase enviarSunat;
	private final Transacciones transacciones;
	private final Clock reloj;

	public EmitirVentaService(GestionarEmpresaUseCase empresas, GestionarSeriesUseCase series,
			GestionarClientesUseCase clientes, GestionarProductosUseCase productos, AjustarStockUseCase stock,
			VentaRepositoryPort ventas, RegistrarDocumentoUseCase registrarSunat, EnviarDocumentoUseCase enviarSunat,
			Transacciones transacciones, Clock reloj) {
		this.empresas = empresas;
		this.series = series;
		this.clientes = clientes;
		this.productos = productos;
		this.stock = stock;
		this.ventas = ventas;
		this.registrarSunat = registrarSunat;
		this.enviarSunat = enviarSunat;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public ResultadoCalculo previsualizar(SolicitudCalculo solicitud) {
		Empresa empresa = empresas.obtener();
		Moneda moneda = solicitud.moneda() == null ? Moneda.PEN : solicitud.moneda();
		return CalculoVentas.calcular(productos, solicitud.items(),
				solicitud.descuentoGlobal() == null ? DescuentoGlobal.ninguno() : solicitud.descuentoGlobal(), moneda,
				moneda == Moneda.PEN ? java.math.BigDecimal.ONE : solicitud.tipoCambio(), empresa.porcentajeIgv(),
				LocalDate.now(reloj));
	}

	@Override
	public Venta emitir(SolicitudEmision solicitud) {
		Venta emitida = transacciones.ejecutar(() -> persistir(solicitud));
		if (emitida.tipo().electronico()) {
			try {
				enviarSunat.enviar(emitida.tipo(), emitida.serie(), emitida.correlativo());
			} catch (DominioException e) {
				// La venta ya existe: queda PENDIENTE (sin certificado, sin SOL o SUNAT caída).
			}
			return ventas.buscarPorId(emitida.id()).orElse(emitida);
		}
		return emitida;
	}

	private Venta persistir(SolicitudEmision solicitud) {
		Empresa empresa = empresas.obtener();
		Cliente cliente = clientes.obtener(solicitud.clienteId());
		Moneda moneda = solicitud.moneda() == null ? Moneda.PEN : solicitud.moneda();
		LocalDate fecha = LocalDate.now(reloj);
		ResultadoCalculo calculo = CalculoVentas.calcular(productos, solicitud.items(),
				solicitud.descuentoGlobal() == null ? DescuentoGlobal.ninguno() : solicitud.descuentoGlobal(), moneda,
				solicitud.tipoCambio(), empresa.porcentajeIgv(), fecha);
		List<LineaVenta> lineas = CalculoVentas.lineas(productos, solicitud.items(), calculo.lineas());
		int correlativo = series.siguienteCorrelativo(solicitud.tipo(), solicitud.serie());
		AtomicInteger n = new AtomicInteger(1);
		List<Cuota> cuotas = solicitud.cuotas() == null ? List.of()
				: solicitud.cuotas().stream()
						.map(c -> new Cuota(n.getAndIncrement(), c.fechaPago(), c.monto()))
						.toList();
		Venta venta = new Venta(null, solicitud.tipo(), solicitud.serie(), correlativo, fecha,
				LocalTime.now(reloj).withNano(0), solicitud.fechaVencimiento(), moneda, solicitud.tipoCambio(),
				cliente.id(), cliente.documento(), cliente.nombre(), cliente.direccion(), lineas, calculo.totales(),
				solicitud.formaPago() == null ? FormaPago.CONTADO : solicitud.formaPago(), cuotas,
				empresa.bienesSelva(), empresa.serviciosSelva(), null, solicitud.observacion(), false);
		Venta guardada = ventas.guardar(venta);
		lineas.forEach(linea -> stock.ajustar(linea.productoId(), linea.calculo().cantidad().negate()));
		if (guardada.tipo().electronico()) {
			registrarSunat.registrar(aComprobante(guardada));
		}
		return guardada;
	}

	private static ComprobanteElectronico aComprobante(Venta venta) {
		return new ComprobanteElectronico(venta.tipo(), venta.serie(), venta.correlativo(), venta.fechaEmision(),
				venta.horaEmision(), venta.fechaVencimiento(), venta.moneda(),
				new ComprobanteElectronico.Receptor(venta.clienteDocumento().tipo(),
						venta.clienteDocumento().numero(), venta.clienteNombre(), venta.clienteDireccion()),
				venta.lineas().stream()
						.map(l -> new ComprobanteElectronico.Linea(l.codigo(), l.descripcion(), l.unidadMedida(),
								l.calculo()))
						.toList(),
				venta.totales(),
				venta.cuotas().stream().map(c -> new ComprobanteElectronico.Cuota(c.monto(), c.fechaPago())).toList(),
				venta.bienesSelva(), venta.serviciosSelva(), null);
	}

}
