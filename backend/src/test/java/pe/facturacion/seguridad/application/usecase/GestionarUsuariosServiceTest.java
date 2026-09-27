package pe.facturacion.seguridad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase.CambiosUsuario;
import pe.facturacion.seguridad.application.port.in.GestionarUsuariosUseCase.NuevoUsuario;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;
import pe.facturacion.shared.domain.exception.DominioException;

@ExtendWith(MockitoExtension.class)
class GestionarUsuariosServiceTest {

	@Mock
	private UsuarioRepositoryPort usuarios;
	@Mock
	private PasswordHasherPort hasher;

	private GestionarUsuariosService servicio;

	@BeforeEach
	void setUp() {
		servicio = new GestionarUsuariosService(usuarios, hasher);
	}

	private static Usuario existente(Long id, Rol rol) {
		return new Usuario(id, "Ana Pérez", "ana", "hash-viejo", null, rol, true, null);
	}

	@Test
	void creaUnUsuarioConLaClaveHasheada() {
		when(usuarios.buscarPorUsername("ana.perez")).thenReturn(Optional.empty());
		when(hasher.hashear("una-clave-bien-larga")).thenReturn("hash");
		when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));

		Usuario creado = servicio.crear(
				new NuevoUsuario("Ana Pérez", "Ana.Perez", null, Rol.VENDEDOR, "una-clave-bien-larga"));

		assertThat(creado.username()).isEqualTo("ana.perez");
		assertThat(creado.passwordHash()).isEqualTo("hash");
		assertThat(creado.activo()).isTrue();
	}

	@Test
	void rechazaUsernamesRepetidosOInvalidos() {
		when(usuarios.buscarPorUsername("ana")).thenReturn(Optional.of(existente(1L, Rol.VENDEDOR)));

		assertThatThrownBy(() -> servicio.crear(new NuevoUsuario("Ana", "ana", null, Rol.VENDEDOR, "clave-muy-larga")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("usuario-duplicado");
		assertThatThrownBy(() -> servicio.crear(new NuevoUsuario("Ana", "a b", null, Rol.VENDEDOR, "clave-muy-larga")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("username-invalido");
	}

	@Test
	void exigeUnaClaveDeDoceCaracteres() {
		assertThatThrownBy(() -> servicio.crear(new NuevoUsuario("Ana", "ana", null, Rol.VENDEDOR, "corta")))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("password-corta");
		verify(usuarios, never()).guardar(any());
	}

	@Test
	void unAdministradorNoPuedeDesactivarseNiQuitarseElRol() {
		when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(existente(1L, Rol.ADMINISTRADOR)));

		assertThatThrownBy(() -> servicio.actualizar(1L, new CambiosUsuario("Ana", null, Rol.ADMINISTRADOR, false), 1L))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("autobloqueo");
		assertThatThrownBy(() -> servicio.actualizar(1L, new CambiosUsuario("Ana", null, Rol.VENDEDOR, true), 1L))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("autobloqueo");
	}

	@Test
	void actualizaAOtroUsuarioConservandoSuClave() {
		when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(existente(2L, Rol.VENDEDOR)));
		when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));

		Usuario actualizado = servicio.actualizar(2L,
				new CambiosUsuario("Ana María", "ana@empresa.pe", Rol.ESPECIAL, false), 1L);

		assertThat(actualizado.rol()).isEqualTo(Rol.ESPECIAL);
		assertThat(actualizado.activo()).isFalse();
		assertThat(actualizado.passwordHash()).isEqualTo("hash-viejo");
	}

	@Test
	void cambiaLaClaveHasheandola() {
		when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(existente(2L, Rol.VENDEDOR)));
		when(hasher.hashear("otra-clave-bien-larga")).thenReturn("hash-nuevo");

		servicio.cambiarPassword(2L, "otra-clave-bien-larga");

		ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarios).guardar(guardado.capture());
		assertThat(guardado.getValue().passwordHash()).isEqualTo("hash-nuevo");
	}

}
