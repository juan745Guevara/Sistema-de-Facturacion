package pe.facturacion.ventas.application.usecase;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.domain.model.Venta;

/** Actualiza el estado SUNAT de la venta y devuelve el stock si el comprobante quedó sin validez. */
public class SincronizarEstadoVentaService {

	private final VentaRepositoryPort ventas;
	private final AjustarStockUseCase stock;

	public SincronizarEstadoVentaService(VentaRepositoryPort ventas, AjustarStockUseCase stock) {
		this.ventas = ventas;
		this.stock = stock;
	}

	public void aplicar(EstadoDocumentoCambiado evento) {
		ventas.buscar(evento.tipo(), evento.serie(), evento.correlativo()).ifPresent(venta -> {
			Venta actualizada = venta.conEstadoSunat(evento.estado());
			if (evento.invalidado() && !venta.stockDevuelto()) {
				venta.lineas().forEach(linea -> stock.ajustar(linea.productoId(), linea.calculo().cantidad()));
				actualizada = actualizada.conStockDevuelto();
			}
			ventas.guardar(actualizada);
		});
	}

}
