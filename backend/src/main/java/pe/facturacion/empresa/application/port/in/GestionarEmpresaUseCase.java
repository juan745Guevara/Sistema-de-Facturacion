package pe.facturacion.empresa.application.port.in;

import pe.facturacion.empresa.domain.model.Empresa;

public interface GestionarEmpresaUseCase {

	/** @throws pe.facturacion.empresa.domain.exception.EmpresaNoConfiguradaException si aún no se registró */
	Empresa obtener();

	Empresa guardar(Empresa empresa);

}
