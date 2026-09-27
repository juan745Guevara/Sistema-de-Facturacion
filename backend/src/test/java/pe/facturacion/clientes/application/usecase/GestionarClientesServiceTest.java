package pe.facturacion.clientes.application.usecase;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.clientes.application.port.out.ClienteRepositoryPort;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

@ExtendWith(MockitoExtension.class)
class GestionarClientesServiceTest {

	private static final DocumentoIdentidad RUC = DocumentoIdentidad.ruc("20601487871");

	@Mock
	private ClienteRepositoryPort clientes;

	private GestionarClientesService servicio;

	@BeforeEach
	void setUp() {
		servicio = new GestionarClientesService(clientes);
	}

	private static Cliente cliente(Long id) {
		return new Cliente(id, RUC, " Comercial Andina S.A.C. ", null, "", null, null);
	}

	@Test
	void creaUnClienteConDatosNormalizados() {
		when(clientes.existeDocumento(RUC, null)).thenReturn(false);
		when(clientes.guardar(any())).thenAnswer(i -> i.getArgument(0));

		Cliente creado = servicio.crear(cliente(50L));

		assertThat(creado.id()).isNull();
		assertThat(creado.nombre()).isEqualTo("Comercial Andina S.A.C.");
		assertThat(creado.email()).isNull();
	}

	@Test
	void noPermiteDosClientesConElMismoDocumento() {
		when(clientes.existeDocumento(RUC, null)).thenReturn(true);

		assertThatThrownBy(() -> servicio.crear(cliente(null)))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("cliente-duplicado");
		verify(clientes, never()).guardar(any());
	}

	@Test
	void alActualizarExcluyeAlPropioCliente() {
		when(clientes.buscarPorId(7L)).thenReturn(Optional.of(cliente(7L)));
		when(clientes.existeDocumento(RUC, 7L)).thenReturn(false);
		when(clientes.guardar(any())).thenAnswer(i -> i.getArgument(0));

		assertThat(servicio.actualizar(7L, cliente(null)).id()).isEqualTo(7L);
	}

	@Test
	void eliminarUnClienteInexistenteDevuelveNoEncontrado() {
		when(clientes.buscarPorId(7L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.eliminar(7L))
				.isInstanceOf(DominioException.class)
				.extracting("tipo").isEqualTo(TipoError.NO_ENCONTRADO);
		verify(clientes, never()).eliminar(any());
	}

}
