package pe.facturacion.seguridad.application.port.out;

import java.time.Instant;

import pe.facturacion.seguridad.domain.model.Usuario;

public interface TokenEmisorPort {

	TokenEmitido emitir(Usuario usuario);

	record TokenEmitido(String valor, Instant expiraEn) {
	}

}
