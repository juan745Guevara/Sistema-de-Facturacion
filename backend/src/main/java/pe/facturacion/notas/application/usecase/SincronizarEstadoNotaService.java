package pe.facturacion.notas.application.usecase;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;

public class SincronizarEstadoNotaService {

	private final NotaRepositoryPort notas;
	private final AjustarStockUseCase stock;

	public SincronizarEstadoNotaService(NotaRepositoryPort notas, AjustarStockUseCase stock) {
		this.notas = notas;
		this.stock = stock;
	}

	public void aplicar(EstadoDocumentoCambiado evento) {
		notas.buscar(evento.tipo(), evento.serie(), evento.correlativo()).ifPresent(nota -> {
			Nota actualizada = nota.conEstadoSunat(evento.estado());
			if (evento.invalidado() && nota.stockAplicado() && nota.afectaStock()) {
				nota.lineas().forEach(linea -> stock.ajustar(linea.productoId(), linea.calculo().cantidad().negate()));
				actualizada = actualizada.conStockAplicado(false);
			}
			notas.guardar(actualizada);
		});
	}

}
