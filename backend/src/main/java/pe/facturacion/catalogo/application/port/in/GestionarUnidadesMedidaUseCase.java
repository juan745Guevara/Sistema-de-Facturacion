package pe.facturacion.catalogo.application.port.in;

import java.util.List;

import pe.facturacion.catalogo.domain.model.UnidadMedida;

public interface GestionarUnidadesMedidaUseCase {

	List<UnidadMedida> listar(boolean soloActivas);

	UnidadMedida cambiarActivacion(String codigo, boolean activa);

}
