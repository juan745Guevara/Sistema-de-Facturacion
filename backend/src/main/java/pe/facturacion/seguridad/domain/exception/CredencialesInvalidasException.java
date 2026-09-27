package pe.facturacion.seguridad.domain.exception;

import pe.facturacion.shared.domain.exception.DominioException;

/** Mismo mensaje para usuario inexistente, inactivo o clave errada, para no revelar cuál falló. */
public class CredencialesInvalidasException extends DominioException {

	public CredencialesInvalidasException() {
		super(TipoError.NO_AUTENTICADO, "credenciales-invalidas", "Usuario o contraseña incorrectos");
	}

}
