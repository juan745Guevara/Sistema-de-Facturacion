package pe.facturacion.seguridad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.seguridad.application.dto.SesionIniciada;
import pe.facturacion.seguridad.application.port.in.AutenticarUsuarioUseCase.Credenciales;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort.TokenEmitido;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.exception.CredencialesInvalidasException;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioServiceTest {

	private static final Instant AHORA = Instant.parse("2026-09-27T22:00:00Z");

	@Mock
	private UsuarioRepositoryPort usuarios;
	@Mock
	private PasswordHasherPort hasher;
	@Mock
	private TokenEmisorPort tokens;

	private AutenticarUsuarioService servicio;

	@BeforeEach
	void setUp() {
		servicio = new AutenticarUsuarioService(usuarios, hasher, tokens, Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	@Test
	void autenticaYRegistraElUltimoIngreso() {
		Usuario usuario = usuario(true);
		when(usuarios.buscarPorUsername("vendedor1")).thenReturn(Optional.of(usuario));
		when(hasher.coincide("secreta", "hash")).thenReturn(true);
		when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
		when(tokens.emitir(any())).thenReturn(new TokenEmitido("jwt", AHORA.plusSeconds(3600)));

		SesionIniciada sesion = servicio.autenticar(new Credenciales("  Vendedor1 ", "secreta"));

		assertThat(sesion.token()).isEqualTo("jwt");
		assertThat(sesion.usuario().username()).isEqualTo("vendedor1");
		assertThat(sesion.usuario().rol()).isEqualTo(Rol.VENDEDOR);
		ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarios).guardar(guardado.capture());
		assertThat(guardado.getValue().ultimoLogin()).isEqualTo(AHORA);
	}

	@Test
	void rechazaClaveIncorrecta() {
		when(usuarios.buscarPorUsername("vendedor1")).thenReturn(Optional.of(usuario(true)));
		when(hasher.coincide("mala", "hash")).thenReturn(false);

		assertThatThrownBy(() -> servicio.autenticar(new Credenciales("vendedor1", "mala")))
				.isInstanceOf(CredencialesInvalidasException.class);
		verify(tokens, never()).emitir(any());
	}

	@Test
	void rechazaUsuarioInexistente() {
		when(usuarios.buscarPorUsername("nadie")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.autenticar(new Credenciales("nadie", "x")))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void rechazaUsuarioInactivoSinComprobarClave() {
		when(usuarios.buscarPorUsername("vendedor1")).thenReturn(Optional.of(usuario(false)));

		assertThatThrownBy(() -> servicio.autenticar(new Credenciales("vendedor1", "secreta")))
				.isInstanceOf(CredencialesInvalidasException.class);
		verify(hasher, never()).coincide(any(), any());
	}

	@Test
	void rechazaCredencialesVacias() {
		assertThatThrownBy(() -> servicio.autenticar(new Credenciales(" ", "x")))
				.isInstanceOf(CredencialesInvalidasException.class);
		assertThatThrownBy(() -> servicio.autenticar(new Credenciales("a", "")))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	private static Usuario usuario(boolean activo) {
		return new Usuario(7L, "Vendedor Uno", "vendedor1", "hash", null, Rol.VENDEDOR, activo, null);
	}

}
