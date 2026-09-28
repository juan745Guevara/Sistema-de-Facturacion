package pe.facturacion.ventas.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;
import pe.facturacion.ventas.application.usecase.SincronizarEstadoVentaService;

@Component
@RequiredArgsConstructor
class EstadoSunatListener {

	private final SincronizarEstadoVentaService sincronizar;

	@EventListener
	void alCambiarEstado(EstadoDocumentoCambiado evento) {
		sincronizar.aplicar(evento);
	}

}
