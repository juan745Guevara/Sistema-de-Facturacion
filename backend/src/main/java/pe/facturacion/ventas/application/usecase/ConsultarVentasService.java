package pe.facturacion.ventas.application.usecase;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.domain.model.Venta;

public class ConsultarVentasService implements ConsultarVentasUseCase {

	private final VentaRepositoryPort ventas;

	public ConsultarVentasService(VentaRepositoryPort ventas) {
		this.ventas = ventas;
	}

	@Override
	public Venta obtener(Long id) {
		return ventas.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));
	}

	@Override
	public Pagina<Venta> buscar(Filtro filtro, ConsultaPaginada consulta) {
		return ventas.buscar(filtro == null ? Filtro.todos() : filtro, consulta);
	}

}
