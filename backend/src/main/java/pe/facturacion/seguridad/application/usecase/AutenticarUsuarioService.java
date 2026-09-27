package pe.facturacion.seguridad.application.usecase;

import java.time.Clock;
import java.util.Objects;

import pe.facturacion.seguridad.application.dto.SesionIniciada;
import pe.facturacion.seguridad.application.dto.SesionIniciada.UsuarioAutenticado;
import pe.facturacion.seguridad.application.port.in.AutenticarUsuarioUseCase;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort.TokenEmitido;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.exception.CredencialesInvalidasException;
import pe.facturacion.seguridad.domain.model.Usuario;

public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

	private final UsuarioRepositoryPort usuarios;
	private final PasswordHasherPort hasher;
	private final TokenEmisorPort tokens;
	private final Clock reloj;

	public AutenticarUsuarioService(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher,
			TokenEmisorPort tokens, Clock reloj) {
		this.usuarios = Objects.requireNonNull(usuarios);
		this.hasher = Objects.requireNonNull(hasher);
		this.tokens = Objects.requireNonNull(tokens);
		this.reloj = Objects.requireNonNull(reloj);
	}

	@Override
	public SesionIniciada autenticar(Credenciales credenciales) {
		if (credenciales == null || esVacio(credenciales.username()) || esVacio(credenciales.password())) {
			throw new CredencialesInvalidasException();
		}
		Usuario usuario = usuarios.buscarPorUsername(Usuario.normalizarUsername(credenciales.username()))
				.filter(Usuario::activo)
				.filter(u -> hasher.coincide(credenciales.password(), u.passwordHash()))
				.orElseThrow(CredencialesInvalidasException::new);

		Usuario conIngreso = usuarios.guardar(usuario.registrarIngreso(reloj.instant()));
		TokenEmitido token = tokens.emitir(conIngreso);
		return new SesionIniciada(token.valor(), token.expiraEn(), new UsuarioAutenticado(
				conIngreso.id(), conIngreso.nombre(), conIngreso.username(), conIngreso.rol()));
	}

	private static boolean esVacio(String valor) {
		return valor == null || valor.isBlank();
	}

}
