package pe.facturacion.sunat.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.application.port.out.EmisorPort;
import pe.facturacion.sunat.application.port.out.EventosPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort;
import pe.facturacion.sunat.application.port.out.GeneradorXmlLotePort;
import pe.facturacion.sunat.application.port.out.LoteRepositoryPort;
import pe.facturacion.sunat.application.port.out.ServicioSunatPort;

@ExtendWith(MockitoExtension.class)
class GestionarLotesServiceTest {

	@Mock
	private DocumentoElectronicoRepositoryPort documentos;
	@Mock
	private LoteRepositoryPort lotes;
	@Mock
	private EmisorPort emisores;
	@Mock
	private GeneradorXmlLotePort generador;
	@Mock
	private FirmaDigitalPort firma;
	@Mock
	private ServicioSunatPort sunat;
	@Mock
	private EventosPort eventos;

	private GestionarLotesService servicio;

	@BeforeEach
	void setUp() {
		Clock reloj = Clock.fixed(Instant.parse("2026-03-15T15:00:00Z"), ZoneId.of("America/Lima"));
		Transacciones tx = new Transacciones() {
			@Override
			public <T> T ejecutar(Supplier<T> operacion) {
				return operacion.get();
			}
		};
		servicio = new GestionarLotesService(documentos, lotes, emisores, generador, firma, sunat, eventos, tx, reloj);
	}

	@Test
	void noGeneraResumenSiNoHayBoletasPendientes() {
		when(documentos.listar(TipoComprobante.BOLETA, LocalDate.of(2026, 3, 15), EstadoSunat.PENDIENTE))
				.thenReturn(List.of());

		assertThatThrownBy(() -> servicio.generarResumen(LocalDate.of(2026, 3, 15)))
				.isInstanceOf(DominioException.class)
				.extracting("codigo").isEqualTo("resumen-vacio");
	}

}
