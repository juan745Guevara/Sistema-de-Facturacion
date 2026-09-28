package pe.facturacion.sunat.application.port.out;

import pe.facturacion.sunat.application.event.EstadoDocumentoCambiado;

public interface EventosPort {

	void publicar(EstadoDocumentoCambiado evento);

}
