package pe.facturacion.empresa.domain.exception;

import pe.facturacion.shared.domain.exception.DominioException;

public class EmpresaNoConfiguradaException extends DominioException {

	public EmpresaNoConfiguradaException() {
		super(TipoError.NO_ENCONTRADO, "empresa-no-configurada", "Aún no se registran los datos de la empresa");
	}

}
