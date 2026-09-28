package pe.facturacion.notas.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.facturacion.notas.application.usecase.SincronizarEstadoNotaService;
import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;

@Component
@RequiredArgsConstructor
class EstadoSunatNotaListener {

	private final SincronizarEstadoNotaService sincronizar;

	@EventListener
	void alCambiarEstado(EstadoDocumentoCambiado evento) {
		sincronizar.aplicar(evento);
	}

}
