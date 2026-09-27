package pe.facturacion.catalogo.application.usecase;

import java.util.List;
import java.util.Objects;

import pe.facturacion.catalogo.application.port.in.GestionarUnidadesMedidaUseCase;
import pe.facturacion.catalogo.application.port.out.UnidadMedidaRepositoryPort;
import pe.facturacion.catalogo.domain.model.UnidadMedida;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;

public class GestionarUnidadesMedidaService implements GestionarUnidadesMedidaUseCase {

	private final UnidadMedidaRepositoryPort unidades;

	public GestionarUnidadesMedidaService(UnidadMedidaRepositoryPort unidades) {
		this.unidades = Objects.requireNonNull(unidades);
	}

	@Override
	public List<UnidadMedida> listar(boolean soloActivas) {
		return unidades.listar(soloActivas);
	}

	@Override
	public UnidadMedida cambiarActivacion(String codigo, boolean activa) {
		UnidadMedida unidad = unidades.buscarPorCodigo(codigo)
				.orElseThrow(() -> new RecursoNoEncontradoException("Unidad de medida", codigo));
		return unidades.guardar(unidad.conActivacion(activa));
	}

}
