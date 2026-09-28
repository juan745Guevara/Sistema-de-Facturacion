package pe.facturacion.empresa.application.port.in;

import java.util.List;

import pe.facturacion.empresa.domain.model.Serie;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface GestionarSeriesUseCase {

	List<Serie> listar(TipoComprobante tipo);

	Serie crear(TipoComprobante tipo, String serie, int ultimoCorrelativo);

	Serie cambiarActivacion(Long id, boolean activa);

	/**
	 * Reserva el siguiente número de la serie. Debe llamarse dentro de la transacción que guarda
	 * el documento: la fila queda bloqueada hasta el commit y el número no se repite.
	 */
	int siguienteCorrelativo(TipoComprobante tipo, String serie);

}
