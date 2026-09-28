package pe.facturacion.notas.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.domain.model.Direccion;
import pe.facturacion.empresa.domain.model.Empresa;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase.SolicitudEmision;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.domain.model.FormaPago;
import pe.facturacion.ventas.domain.model.LineaVenta;
import pe.facturacion.ventas.domain.model.Venta;

@ExtendWith(MockitoExtension.class)
class EmitirNotaServiceTest {

	@Mock
	private GestionarEmpresaUseCase empresas;
	@Mock
	private GestionarSeriesUseCase series;
	@Mock
	private GestionarProductosUseCase productos;
	@Mock
	private AjustarStockUseCase stock;
	@Mock
	private ConsultarVentasUseCase ventas;
	@Mock
	private NotaRepositoryPort notas;
	@Mock
	private RegistrarDocumentoUseCase registrarSunat;
	@Mock
	private EnviarDocumentoUseCase enviarSunat;

	private EmitirNotaService servicio;

	@BeforeEach
	void setUp() {
		Clock reloj = Clock.fixed(Instant.parse("2026-03-15T15:00:00Z"), ZoneId.of("America/Lima"));
		Transacciones tx = new Transacciones() {
			@Override
			public <T> T ejecutar(Supplier<T> operacion) {
				return operacion.get();
			}
		};
		servicio = new EmitirNotaService(empresas, series, productos, stock, ventas, notas, registrarSunat,
				enviarSunat, tx, reloj);
	}

	@Test
	void copiaLaFacturaAlAnularYDevuelveStock() {
		when(empresas.obtener()).thenReturn(new Empresa("20601487871", "Mi Empresa SAC", null,
				new Direccion("Av. 1", "150101", "LIMA", "LIMA", "LIMA", "PE", "0000"), null, null, null,
				new BigDecimal("18"), false, false));
		when(ventas.obtener(TipoComprobante.FACTURA, "F001", 1)).thenReturn(factura());
		when(productos.obtener(10L)).thenReturn(new Producto(10L, "GAS-1", "Gaseosa", 1L, "NIU",
				TipoAfectacionIgv.GRAVADO_ONEROSO, new BigDecimal("118.00"), BigDecimal.ZERO, new BigDecimal("20")));
		when(series.siguienteCorrelativo(TipoComprobante.NOTA_CREDITO, "FC01")).thenReturn(3);
		when(notas.totalAcreditado(any(), any(), anyInt())).thenReturn(BigDecimal.ZERO);
		when(notas.guardar(any())).thenAnswer(i -> ((Nota) i.getArgument(0)).conId(8L));
		when(notas.buscarPorId(8L)).thenReturn(Optional.empty());

		servicio.emitir(new SolicitudEmision(TipoComprobante.NOTA_CREDITO, "FC01", TipoComprobante.FACTURA, "F001", 1,
				"01", List.of(), null, null));

		verify(stock).ajustar(10L, BigDecimal.ONE);
		verify(registrarSunat).registrar(any());
		verify(enviarSunat).enviar(TipoComprobante.NOTA_CREDITO, "FC01", 3);
	}

	private static Venta factura() {
		LineaCalculada linea = new LineaCalculada(BigDecimal.ONE, TipoAfectacionIgv.GRAVADO_ONEROSO,
				new BigDecimal("118"), new BigDecimal("100"), new BigDecimal("18"), BigDecimal.ZERO,
				new BigDecimal("100"), new BigDecimal("18"), BigDecimal.ZERO, BigDecimal.ZERO);
		TotalesComprobante totales = new TotalesComprobante(new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("18"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100"),
				new BigDecimal("118"));
		return new Venta(1L, TipoComprobante.FACTURA, "F001", 1, LocalDate.of(2026, 3, 15), LocalTime.NOON, null,
				Moneda.PEN, BigDecimal.ONE, 5L, DocumentoIdentidad.ruc("20601487871"), "Cliente", null,
				List.of(new LineaVenta(10L, "GAS-1", "Gaseosa", "NIU", linea)), totales, FormaPago.CONTADO, List.of(),
				false, false, EstadoSunat.PENDIENTE, null, false);
	}

}
