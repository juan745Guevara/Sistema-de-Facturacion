package pe.facturacion.compras.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pe.facturacion.catalogo.application.port.in.AjustarStockUseCase;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.domain.model.Producto;
import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase;
import pe.facturacion.compras.application.port.out.CompraRepositoryPort;
import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.compras.domain.model.LineaCompra;
import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.proveedores.application.port.in.GestionarProveedoresUseCase;
import pe.facturacion.proveedores.domain.model.Proveedor;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

public class RegistrarCompraService implements RegistrarCompraUseCase {

	private final GestionarProveedoresUseCase proveedores;
	private final GestionarProductosUseCase productos;
	private final GestionarEmpresaUseCase empresas;
	private final AjustarStockUseCase stock;
	private final CompraRepositoryPort compras;

	public RegistrarCompraService(GestionarProveedoresUseCase proveedores, GestionarProductosUseCase productos,
			GestionarEmpresaUseCase empresas, AjustarStockUseCase stock, CompraRepositoryPort compras) {
		this.proveedores = proveedores;
		this.productos = productos;
		this.empresas = empresas;
		this.stock = stock;
		this.compras = compras;
	}

	@Override
	public Compra registrar(Solicitud solicitud) {
		if (compras.existe(solicitud.tipo().codigo(), solicitud.serie(), solicitud.correlativo())) {
			throw DominioException.conflicto("compra-duplicada", "Ya existe esa compra del proveedor");
		}
		Proveedor proveedor = proveedores.obtener(solicitud.proveedorId());
		BigDecimal tasa = empresas.obtener().porcentajeIgv().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
		List<LineaCompra> lineas = new ArrayList<>();
		BigDecimal gravadas = BigDecimal.ZERO;
		BigDecimal igv = BigDecimal.ZERO;
		for (Item item : solicitud.items()) {
			Producto producto = productos.obtener(item.productoId());
			BigDecimal precio = item.precioUnitario() != null ? item.precioUnitario() : producto.precioCompra();
			BigDecimal valor = precio.multiply(item.cantidad()).setScale(2, RoundingMode.HALF_UP);
			BigDecimal impuesto = valor.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
			lineas.add(new LineaCompra(producto.id(), producto.codigo(), producto.descripcion(), producto.unidadMedida(),
					item.cantidad(), precio, valor, impuesto));
			gravadas = gravadas.add(valor);
			igv = igv.add(impuesto);
		}
		Compra compra = new Compra(null, solicitud.tipo(), solicitud.serie(), solicitud.correlativo(),
				solicitud.fechaEmision() == null ? LocalDate.now() : solicitud.fechaEmision(), proveedor.id(),
				proveedor.documento(), proveedor.nombre(), Moneda.PEN, lineas, gravadas, igv, gravadas.add(igv), false,
				solicitud.observacion());
		Compra guardada = compras.guardar(compra);
		lineas.forEach(l -> stock.ajustar(l.productoId(), l.cantidad()));
		return guardada;
	}

	@Override
	public Compra anular(Long id) {
		Compra compra = obtener(id);
		Compra anulada = compras.guardar(compra.marcarAnulada());
		compra.lineas().forEach(l -> stock.ajustar(l.productoId(), l.cantidad().negate()));
		return anulada;
	}

	@Override
	public Compra obtener(Long id) {
		return compras.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Compra", id));
	}

	@Override
	public Pagina<Compra> buscar(ConsultaPaginada consulta) {
		return compras.buscar(consulta);
	}

}
