package pe.facturacion.seguridad.application.dto;

import java.time.Instant;

import pe.facturacion.seguridad.domain.model.Rol;

public record SesionIniciada(String token, Instant expiraEn, UsuarioAutenticado usuario) {

	public record UsuarioAutenticado(Long id, String nombre, String username, Rol rol) {
	}

}
