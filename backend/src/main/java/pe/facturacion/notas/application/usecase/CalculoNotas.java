package pe.facturacion.notas.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase.ItemSolicitado;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.DescuentoGlobal;
import pe.facturacion.shared.domain.model.calculo.ItemCalculo;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.ResultadoCalculo;
import pe.facturacion.shared.domain.model.sunat.Icbper;
import pe.facturacion.shared.domain.service.CalculadoraComprobante;
import pe.facturacion.ventas.domain.model.LineaVenta;

final class CalculoNotas {

	private CalculoNotas() {
	}

	static ResultadoCalculo calcular(GestionarProductosUseCase productos, List<ItemSolicitado> items,
			DescuentoGlobal descuento, BigDecimal porcentajeIgv, LocalDate fecha) {
		if (items == null || items.isEmpty()) {
			throw DominioException.reglaNegocio("sin-items", "La nota debe tener al menos una línea");
		}
		List<ItemCalculo> calculables = new ArrayList<>(items.size());
		for (ItemSolicitado item : items) {
			Producto producto = productos.obtener(item.productoId());
			BigDecimal precio = item.precioUnitario() != null ? item.precioUnitario() : producto.precioVenta();
			calculables.add(new ItemCalculo(item.cantidad(), precio, producto.tipoAfectacionIgv(), item.descuento(),
					Boolean.TRUE.equals(item.icbper())));
		}
		return CalculadoraComprobante.calcular(calculables, porcentajeIgv,
				descuento == null ? DescuentoGlobal.ninguno() : descuento, Icbper.montoPorBolsa(fecha));
	}

	static List<LineaVenta> lineas(GestionarProductosUseCase productos, List<ItemSolicitado> items,
			List<LineaCalculada> calculadas) {
		List<LineaVenta> lineas = new ArrayList<>(items.size());
		for (int i = 0; i < items.size(); i++) {
			Producto producto = productos.obtener(items.get(i).productoId());
			lineas.add(new LineaVenta(producto.id(), producto.codigo(), producto.descripcion(), producto.unidadMedida(),
					calculadas.get(i)));
		}
		return List.copyOf(lineas);
	}

	static List<ItemSolicitado> desdeLineas(List<LineaVenta> lineas) {
		return lineas.stream()
				.map(l -> new ItemSolicitado(l.productoId(), l.calculo().cantidad(), l.calculo().precioUnitario(),
						l.calculo().descuento(), l.calculo().conIcbper()))
				.toList();
	}

}
