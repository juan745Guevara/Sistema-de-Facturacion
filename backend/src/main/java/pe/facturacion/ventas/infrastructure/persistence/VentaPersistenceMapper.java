package pe.facturacion.ventas.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.ventas.domain.model.Cuota;
import pe.facturacion.ventas.domain.model.LineaVenta;
import pe.facturacion.ventas.domain.model.Venta;

@Component
class VentaPersistenceMapper {

	Venta aDominio(VentaJpaEntity entidad) {
		List<LineaVenta> lineas = entidad.getLineas().stream().map(this::linea).toList();
		List<Cuota> cuotas = entidad.getCuotas().stream()
				.map(c -> new Cuota(c.getNumero(), c.getFechaPago(), c.getMonto()))
				.toList();
		TotalesComprobante totales = new TotalesComprobante(entidad.getGravadas(), entidad.getGravadasIvap(),
				entidad.getExoneradas(), entidad.getInafectas(), entidad.getExportacion(), entidad.getGratuitas(),
				entidad.getDescuentoGlobal(), entidad.getFactorDescuento(), entidad.getIgv(), entidad.getIvap(),
				entidad.getIgvGratuitas(), entidad.getIcbper(), entidad.getValorVenta(), entidad.getTotal());
		return new Venta(entidad.getId(), entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(),
				entidad.getFechaEmision(), entidad.getHoraEmision(), entidad.getFechaVencimiento(), entidad.getMoneda(),
				entidad.getTipoCambio(), entidad.getClienteId(),
				new DocumentoIdentidad(entidad.getClienteTipoDocumento(), entidad.getClienteNumeroDocumento()),
				entidad.getClienteNombre(), entidad.getClienteDireccion(), lineas, totales, entidad.getFormaPago(),
				cuotas, entidad.isBienesSelva(), entidad.isServiciosSelva(), entidad.getEstadoSunat(),
				entidad.getObservacion(), entidad.isStockDevuelto());
	}

	void copiar(Venta venta, VentaJpaEntity entidad) {
		entidad.setTipo(venta.tipo());
		entidad.setSerie(venta.serie());
		entidad.setCorrelativo(venta.correlativo());
		entidad.setFechaEmision(venta.fechaEmision());
		entidad.setHoraEmision(venta.horaEmision());
		entidad.setFechaVencimiento(venta.fechaVencimiento());
		entidad.setMoneda(venta.moneda());
		entidad.setTipoCambio(venta.tipoCambio());
		entidad.setClienteId(venta.clienteId());
		entidad.setClienteTipoDocumento(venta.clienteDocumento().tipo());
		entidad.setClienteNumeroDocumento(venta.clienteDocumento().numero());
		entidad.setClienteNombre(venta.clienteNombre());
		entidad.setClienteDireccion(venta.clienteDireccion());
		entidad.setGravadas(venta.totales().gravadas());
		entidad.setGravadasIvap(venta.totales().gravadasIvap());
		entidad.setExoneradas(venta.totales().exoneradas());
		entidad.setInafectas(venta.totales().inafectas());
		entidad.setExportacion(venta.totales().exportacion());
		entidad.setGratuitas(venta.totales().gratuitas());
		entidad.setDescuentoGlobal(venta.totales().descuentoGlobal());
		entidad.setFactorDescuento(venta.totales().factorDescuentoGlobal());
		entidad.setIgv(venta.totales().igv());
		entidad.setIvap(venta.totales().ivap());
		entidad.setIgvGratuitas(venta.totales().igvGratuitas());
		entidad.setIcbper(venta.totales().icbper());
		entidad.setValorVenta(venta.totales().valorVenta());
		entidad.setTotal(venta.totales().total());
		entidad.setFormaPago(venta.formaPago());
		entidad.setBienesSelva(venta.bienesSelva());
		entidad.setServiciosSelva(venta.serviciosSelva());
		entidad.setEstadoSunat(venta.estadoSunat());
		entidad.setObservacion(venta.observacion());
		entidad.setStockDevuelto(venta.stockDevuelto());
		entidad.getLineas().clear();
		List<LineaVentaJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaVenta linea : venta.lineas()) {
			lineas.add(aEntidad(linea, entidad, orden++));
		}
		entidad.getLineas().addAll(lineas);
		entidad.getCuotas().clear();
		List<CuotaVentaJpaEntity> cuotas = new ArrayList<>();
		for (Cuota cuota : venta.cuotas()) {
			CuotaVentaJpaEntity fila = new CuotaVentaJpaEntity();
			fila.setVenta(entidad);
			fila.setNumero(cuota.numero());
			fila.setFechaPago(cuota.fechaPago());
			fila.setMonto(cuota.monto());
			cuotas.add(fila);
		}
		entidad.getCuotas().addAll(cuotas);
	}

	private LineaVenta linea(LineaVentaJpaEntity entidad) {
		return new LineaVenta(entidad.getProductoId(), entidad.getCodigo(), entidad.getDescripcion(),
				entidad.getUnidadMedida(),
				new LineaCalculada(entidad.getCantidad(), entidad.getAfectacion(), entidad.getPrecioUnitario(),
						entidad.getValorUnitario(), entidad.getPorcentajeImpuesto(), entidad.getDescuento(),
						entidad.getValorVenta(), entidad.getImpuesto(), entidad.getIcbper(),
						entidad.getIcbperPorBolsa()));
	}

	private static LineaVentaJpaEntity aEntidad(LineaVenta linea, VentaJpaEntity venta, int orden) {
		LineaVentaJpaEntity entidad = new LineaVentaJpaEntity();
		entidad.setVenta(venta);
		entidad.setProductoId(linea.productoId());
		entidad.setCodigo(linea.codigo());
		entidad.setDescripcion(linea.descripcion());
		entidad.setUnidadMedida(linea.unidadMedida());
		LineaCalculada c = linea.calculo();
		entidad.setCantidad(c.cantidad());
		entidad.setAfectacion(c.afectacion());
		entidad.setPrecioUnitario(c.precioUnitario());
		entidad.setValorUnitario(c.valorUnitario());
		entidad.setPorcentajeImpuesto(c.porcentajeImpuesto());
		entidad.setDescuento(c.descuento());
		entidad.setValorVenta(c.valorVenta());
		entidad.setImpuesto(c.impuesto());
		entidad.setIcbper(c.icbper());
		entidad.setIcbperPorBolsa(c.icbperPorBolsa());
		entidad.setOrden(orden);
		return entidad;
	}

}
