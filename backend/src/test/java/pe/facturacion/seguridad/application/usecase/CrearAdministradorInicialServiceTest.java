package pe.facturacion.seguridad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.seguridad.application.port.in.CrearAdministradorInicialUseCase.DatosAdministrador;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;
import pe.facturacion.seguridad.application.port.out.UsuarioRepositoryPort;
import pe.facturacion.seguridad.domain.model.Rol;
import pe.facturacion.seguridad.domain.model.Usuario;
import pe.facturacion.shared.domain.exception.DominioException;

@ExtendWith(MockitoExtension.class)
class CrearAdministradorInicialServiceTest {

	@Mock
	private UsuarioRepositoryPort usuarios;
	@Mock
	private PasswordHasherPort hasher;

	private CrearAdministradorInicialService servicio;

	@BeforeEach
	void setUp() {
		servicio = new CrearAdministradorInicialService(usuarios, hasher);
	}

	@Test
	void creaAdministradorConClaveHasheadaSiNoHayUsuarios() {
		when(usuarios.existeAlguno()).thenReturn(false);
		when(hasher.hashear("una-clave-larga")).thenReturn("hash");

		boolean creado = servicio.crearSiNoHayUsuarios(new DatosAdministrador("Admin", "Admin", "una-clave-larga"));

		assertThat(creado).isTrue();
		ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarios).guardar(guardado.capture());
		assertThat(guardado.getValue().username()).isEqualTo("admin");
		assertThat(guardado.getValue().passwordHash()).isEqualTo("hash");
		assertThat(guardado.getValue().rol()).isEqualTo(Rol.ADMINISTRADOR);
		assertThat(guardado.getValue().activo()).isTrue();
	}

	@Test
	void noHaceNadaSiYaHayUsuarios() {
		when(usuarios.existeAlguno()).thenReturn(true);

		assertThat(servicio.crearSiNoHayUsuarios(new DatosAdministrador("Admin", "admin", "corta"))).isFalse();
		verify(usuarios, never()).guardar(any());
	}

	@Test
	void exigeUnaClaveMinima() {
		when(usuarios.existeAlguno()).thenReturn(false);

		assertThatThrownBy(() -> servicio.crearSiNoHayUsuarios(new DatosAdministrador("Admin", "admin", "corta")))
				.isInstanceOf(DominioException.class)
				.hasMessageContaining("12");
		verify(usuarios, never()).guardar(any());
	}

}
