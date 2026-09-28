package pe.facturacion.reportes.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase;
import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase.Filtro;
import pe.facturacion.ventas.domain.model.Venta;

public class ConsultarReportesService implements ConsultarReportesUseCase {

	private final ConsultarVentasUseCase ventas;
	private final RegistrarCompraUseCase compras;
	private final ConsultarDocumentosUseCase documentos;
	private final Clock reloj;

	public ConsultarReportesService(ConsultarVentasUseCase ventas, RegistrarCompraUseCase compras,
			ConsultarDocumentosUseCase documentos, Clock reloj) {
		this.ventas = ventas;
		this.compras = compras;
		this.documentos = documentos;
		this.reloj = reloj;
	}

	@Override
	public Resumen periodoVentas(LocalDate desde, LocalDate hasta) {
		var pagina = ventas.buscar(new Filtro(null, desde, hasta), new ConsultaPaginada(null, 0, 100));
		List<Fila> filas = new ArrayList<>();
		BigDecimal gravadas = BigDecimal.ZERO;
		BigDecimal igv = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		for (Venta venta : pagina.contenido()) {
			filas.add(new Fila(venta.tipo().descripcion(), venta.numero(), venta.fechaEmision(), venta.clienteNombre(),
					venta.totales().total(), venta.estadoSunat().name()));
			gravadas = gravadas.add(venta.totales().gravadas());
			igv = igv.add(venta.totales().igv());
			total = total.add(venta.totales().total());
		}
		return new Resumen(desde, hasta, pagina.totalElementos(), gravadas, igv, total, filas);
	}

	@Override
	public Resumen periodoCompras(LocalDate desde, LocalDate hasta) {
		var pagina = compras.buscar(new ConsultaPaginada(null, 0, 100));
		List<Fila> filas = new ArrayList<>();
		BigDecimal gravadas = BigDecimal.ZERO;
		BigDecimal igv = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		long cantidad = 0;
		for (Compra compra : pagina.contenido()) {
			if (desde != null && compra.fechaEmision().isBefore(desde)) {
				continue;
			}
			if (hasta != null && compra.fechaEmision().isAfter(hasta)) {
				continue;
			}
			if (compra.anulada()) {
				continue;
			}
			cantidad++;
			filas.add(new Fila(compra.tipo().descripcion(), compra.serie() + "-" + compra.correlativo(),
					compra.fechaEmision(), compra.proveedorNombre(), compra.total(),
					compra.anulada() ? "ANULADA" : "REGISTRADA"));
			gravadas = gravadas.add(compra.gravadas());
			igv = igv.add(compra.igv());
			total = total.add(compra.total());
		}
		return new Resumen(desde, hasta, cantidad, gravadas, igv, total, filas);
	}

	@Override
	public Dashboard dashboard() {
		LocalDate hoy = LocalDate.now(reloj);
		Resumen ventasHoy = periodoVentas(hoy, hoy);
		long pendientes = documentos.buscar(new FiltroDocumentos(null, EstadoSunat.PENDIENTE, null, null),
				new ConsultaPaginada(null, 0, 1)).totalElementos();
		LocalDate inicioMes = hoy.withDayOfMonth(1);
		Resumen comprasMes = periodoCompras(inicioMes, hoy);
		return new Dashboard(ventasHoy.total(), pendientes, comprasMes.cantidad(), comprasMes.total());
	}

}
