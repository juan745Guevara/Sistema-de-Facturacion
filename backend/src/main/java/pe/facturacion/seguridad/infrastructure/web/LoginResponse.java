package pe.facturacion.seguridad.infrastructure.web;

import java.time.Instant;

import pe.facturacion.seguridad.application.dto.SesionIniciada;

record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {

	static LoginResponse desde(SesionIniciada sesion) {
		SesionIniciada.UsuarioAutenticado u = sesion.usuario();
		return new LoginResponse(sesion.token(), "Bearer", sesion.expiraEn(),
				new UsuarioResponse(u.id(), u.nombre(), u.username(), u.rol()));
	}

}
