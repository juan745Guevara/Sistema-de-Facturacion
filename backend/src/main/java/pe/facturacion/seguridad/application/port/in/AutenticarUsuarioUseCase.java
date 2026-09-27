package pe.facturacion.seguridad.application.port.in;

import pe.facturacion.seguridad.application.dto.SesionIniciada;

public interface AutenticarUsuarioUseCase {

	SesionIniciada autenticar(Credenciales credenciales);

	record Credenciales(String username, String password) {
	}

}
