package pe.facturacion.ventas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.clientes.application.port.in.GestionarClientesUseCase;
import pe.facturacion.clientes.domain.model.Cliente;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.domain.model.Direccion;
import pe.facturacion.empresa.domain.model.Empresa;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.ItemSolicitado;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase.SolicitudEmision;
import pe.facturacion.ventas.application.port.out.VentaRepositoryPort;
import pe.facturacion.ventas.domain.model.FormaPago;
import pe.facturacion.ventas.domain.model.Venta;

@ExtendWith(MockitoExtension.class)
class EmitirVentaServiceTest {

	@Mock
	private GestionarEmpresaUseCase empresas;
	@Mock
	private GestionarSeriesUseCase series;
	@Mock
	private GestionarClientesUseCase clientes;
	@Mock
	private GestionarProductosUseCase productos;
	@Mock
	private AjustarStockUseCase stock;
	@Mock
	private VentaRepositoryPort ventas;
	@Mock
	private RegistrarDocumentoUseCase registrarSunat;
	@Mock
	private EnviarDocumentoUseCase enviarSunat;

	private EmitirVentaService servicio;

	@BeforeEach
	void setUp() {
		Clock reloj = Clock.fixed(Instant.parse("2026-03-15T15:00:00Z"), ZoneId.of("America/Lima"));
		Transacciones tx = new Transacciones() {
			@Override
			public <T> T ejecutar(Supplier<T> operacion) {
				return operacion.get();
			}
		};
		servicio = new EmitirVentaService(empresas, series, clientes, productos, stock, ventas, registrarSunat,
				enviarSunat, tx, reloj);
	}

	@Test
	void emiteUnaNotaDeVentaDescuentaStockYNoHablaConSunat() {
		preparar(DocumentoIdentidad.dni("47204426"));
		when(series.siguienteCorrelativo(TipoComprobante.NOTA_VENTA, "N001")).thenReturn(7);
		when(ventas.guardar(any())).thenAnswer(i -> ((Venta) i.getArgument(0)).conId(99L));

		Venta venta = servicio.emitir(solicitud(TipoComprobante.NOTA_VENTA, "N001"));

		assertThat(venta.correlativo()).isEqualTo(7);
		assertThat(venta.totales().total()).isEqualByComparingTo("118.00");
		verify(stock).ajustar(10L, new BigDecimal("-1"));
		verify(registrarSunat, never()).registrar(any());
		verify(enviarSunat, never()).enviar(any(), any(), any(Integer.class));
	}

	@Test
	void registraYEnviaUnaFactura() {
		preparar(DocumentoIdentidad.ruc("20601487871"));
		when(series.siguienteCorrelativo(TipoComprobante.FACTURA, "F001")).thenReturn(1);
		when(ventas.guardar(any())).thenAnswer(i -> ((Venta) i.getArgument(0)).conId(1L));

		servicio.emitir(solicitud(TipoComprobante.FACTURA, "F001"));

		verify(registrarSunat).registrar(any());
		verify(enviarSunat).enviar(TipoComprobante.FACTURA, "F001", 1);
	}

	private void preparar(DocumentoIdentidad documento) {
		when(empresas.obtener()).thenReturn(new Empresa("20601487871", "Mi Empresa SAC", null,
				new Direccion("Av. 1", "150101", "LIMA", "LIMA", "LIMA", "PE", "0000"), null, null, null,
				new BigDecimal("18"), false, false));
		when(clientes.obtener(5L)).thenReturn(new Cliente(5L, documento, "Cliente Demo", "Av. 2", null, null, null));
		when(productos.obtener(10L)).thenReturn(new Producto(10L, "GAS-1", "Gaseosa", 1L, "NIU",
				TipoAfectacionIgv.GRAVADO_ONEROSO, new BigDecimal("118.00"), BigDecimal.ZERO, new BigDecimal("20")));
	}

	private static SolicitudEmision solicitud(TipoComprobante tipo, String serie) {
		return new SolicitudEmision(tipo, serie, 5L, Moneda.PEN, null, null, null, FormaPago.CONTADO, List.of(),
				List.of(new ItemSolicitado(10L, BigDecimal.ONE, null, null, false)), null);
	}

}
