package pe.facturacion.notas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.domain.model.Empresa;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.notas.domain.model.ReferenciaComprobante;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.MotivoNotaCredito;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.domain.model.LineaVenta;
import pe.facturacion.ventas.domain.model.Venta;

public class EmitirNotaService implements EmitirNotaUseCase {

	private final GestionarEmpresaUseCase empresas;
	private final GestionarSeriesUseCase series;
	private final GestionarProductosUseCase productos;
	private final AjustarStockUseCase stock;
	private final ConsultarVentasUseCase ventas;
	private final NotaRepositoryPort notas;
	private final RegistrarDocumentoUseCase registrarSunat;
	private final EnviarDocumentoUseCase enviarSunat;
	private final Transacciones transacciones;
	private final Clock reloj;

	public EmitirNotaService(GestionarEmpresaUseCase empresas, GestionarSeriesUseCase series,
			GestionarProductosUseCase productos, AjustarStockUseCase stock, ConsultarVentasUseCase ventas,
			NotaRepositoryPort notas, RegistrarDocumentoUseCase registrarSunat, EnviarDocumentoUseCase enviarSunat,
			Transacciones transacciones, Clock reloj) {
		this.empresas = empresas;
		this.series = series;
		this.productos = productos;
		this.stock = stock;
		this.ventas = ventas;
		this.notas = notas;
		this.registrarSunat = registrarSunat;
		this.enviarSunat = enviarSunat;
		this.transacciones = transacciones;
		this.reloj = reloj;
	}

	@Override
	public ResultadoCalculo previsualizar(SolicitudCalculo solicitud) {
		Venta origen = ventas.obtener(solicitud.tipoReferencia(), solicitud.serieReferencia(),
				solicitud.correlativoReferencia());
		List<ItemSolicitado> items = itemsEfectivos(origen, TipoComprobante.NOTA_CREDITO, solicitud.codigoMotivo(),
				solicitud.items());
		return CalculoNotas.calcular(productos, items, solicitud.descuentoGlobal(), empresas.obtener().porcentajeIgv(),
				LocalDate.now(reloj));
	}

	@Override
	public Nota emitir(SolicitudEmision solicitud) {
		Nota emitida = transacciones.ejecutar(() -> persistir(solicitud));
		try {
			enviarSunat.enviar(emitida.tipo(), emitida.serie(), emitida.correlativo());
		} catch (DominioException e) {
			// Queda PENDIENTE si no hay certificado, SOL o SUNAT no responde.
		}
		return notas.buscarPorId(emitida.id()).orElse(emitida);
	}

	private Nota persistir(SolicitudEmision solicitud) {
		Empresa empresa = empresas.obtener();
		Venta origen = ventas.obtener(solicitud.tipoReferencia(), solicitud.serieReferencia(),
				solicitud.correlativoReferencia());
		validarOrigen(origen);
		List<ItemSolicitado> items = itemsEfectivos(origen, solicitud.tipo(), solicitud.codigoMotivo(),
				solicitud.items());
		ResultadoCalculo calculo = CalculoNotas.calcular(productos, items, solicitud.descuentoGlobal(),
				empresa.porcentajeIgv(), LocalDate.now(reloj));
		if (solicitud.tipo() == TipoComprobante.NOTA_CREDITO) {
			validarTopeCredito(origen, calculo.totales().total());
		}
		List<LineaVenta> lineas = CalculoNotas.lineas(productos, items, calculo.lineas());
		int correlativo = series.siguienteCorrelativo(solicitud.tipo(), solicitud.serie());
		String descripcionMotivo = solicitud.tipo() == TipoComprobante.NOTA_CREDITO
				? MotivoNotaCredito.desdeCodigo(solicitud.codigoMotivo()).descripcion()
				: pe.facturacion.shared.domain.model.sunat.MotivoNotaDebito.desdeCodigo(solicitud.codigoMotivo())
						.descripcion();
		Nota nota = new Nota(null, solicitud.tipo(), solicitud.serie(), correlativo, LocalDate.now(reloj),
				LocalTime.now(reloj).withNano(0), origen.moneda(), origen.tipoCambio(), origen.clienteId(),
				origen.clienteDocumento(), origen.clienteNombre(), origen.clienteDireccion(),
				new ReferenciaComprobante(origen.tipo(), origen.serie(), origen.correlativo()),
				solicitud.codigoMotivo(), descripcionMotivo, lineas, calculo.totales(), EstadoSunat.PENDIENTE,
				solicitud.observacion(), false);
		Nota guardada = notas.guardar(nota);
		if (guardada.afectaStock()) {
			lineas.forEach(linea -> stock.ajustar(linea.productoId(), linea.calculo().cantidad()));
			guardada = notas.guardar(guardada.conStockAplicado(true));
		}
		registrarSunat.registrar(aComprobante(guardada));
		return guardada;
	}

	private static void validarOrigen(Venta origen) {
		if (origen.estadoSunat() == EstadoSunat.RECHAZADO || origen.estadoSunat() == EstadoSunat.ANULADO) {
			throw DominioException.reglaNegocio("comprobante-sin-valor",
					"No se puede emitir una nota sobre un comprobante rechazado o anulado");
		}
		if (origen.tipo() != TipoComprobante.FACTURA && origen.tipo() != TipoComprobante.BOLETA) {
			throw DominioException.reglaNegocio("referencia-invalida",
					"La nota solo puede referenciar una factura o una boleta");
		}
	}

	private void validarTopeCredito(Venta origen, BigDecimal totalNota) {
		BigDecimal ya = notas.totalAcreditado(origen.tipo(), origen.serie(), origen.correlativo());
		if (ya.add(totalNota).compareTo(origen.totales().total()) > 0) {
			throw DominioException.reglaNegocio("credito-excede-origen",
					"El total de las notas de crédito no puede superar el del comprobante original");
		}
	}

	private static List<ItemSolicitado> itemsEfectivos(Venta origen, TipoComprobante tipoNota, String codigoMotivo,
			List<ItemSolicitado> items) {
		if (items != null && !items.isEmpty()) {
			return items;
		}
		if (tipoNota == TipoComprobante.NOTA_CREDITO && codigoMotivo != null
				&& !MotivoNotaCredito.desdeCodigo(codigoMotivo).copiaComprobanteCompleto()) {
			throw DominioException.reglaNegocio("sin-items", "Este motivo exige indicar las líneas de la nota");
		}
		return CalculoNotas.desdeLineas(origen.lineas());
	}

	private static ComprobanteElectronico aComprobante(Nota nota) {
		return new ComprobanteElectronico(nota.tipo(), nota.serie(), nota.correlativo(), nota.fechaEmision(),
				nota.horaEmision(), null, nota.moneda(),
				new ComprobanteElectronico.Receptor(nota.clienteDocumento().tipo(), nota.clienteDocumento().numero(),
						nota.clienteNombre(), nota.clienteDireccion()),
				nota.lineas().stream()
						.map(l -> new ComprobanteElectronico.Linea(l.codigo(), l.descripcion(), l.unidadMedida(),
								l.calculo()))
						.toList(),
				nota.totales(), List.of(), false, false,
				new ComprobanteElectronico.Referencia(nota.referencia().tipo(), nota.referencia().serie(),
						nota.referencia().correlativo(), nota.codigoMotivo(), nota.descripcionMotivo()));
	}

}
