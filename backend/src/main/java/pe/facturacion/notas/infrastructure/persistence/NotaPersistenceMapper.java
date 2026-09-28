package pe.facturacion.notas.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.notas.domain.model.ReferenciaComprobante;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.ventas.domain.model.LineaVenta;

@Component
class NotaPersistenceMapper {

	Nota aDominio(NotaJpaEntity entidad) {
		List<LineaVenta> lineas = entidad.getLineas().stream().map(this::linea).toList();
		TotalesComprobante totales = new TotalesComprobante(entidad.getGravadas(), entidad.getGravadasIvap(),
				entidad.getExoneradas(), entidad.getInafectas(), entidad.getExportacion(), entidad.getGratuitas(),
				entidad.getDescuentoGlobal(), entidad.getFactorDescuento(), entidad.getIgv(), entidad.getIvap(),
				entidad.getIgvGratuitas(), entidad.getIcbper(), entidad.getValorVenta(), entidad.getTotal());
		return new Nota(entidad.getId(), entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(),
				entidad.getFechaEmision(), entidad.getHoraEmision(), entidad.getMoneda(), entidad.getTipoCambio(),
				entidad.getClienteId(),
				new DocumentoIdentidad(entidad.getClienteTipoDocumento(), entidad.getClienteNumeroDocumento()),
				entidad.getClienteNombre(), entidad.getClienteDireccion(),
				new ReferenciaComprobante(entidad.getTipoReferencia(), entidad.getSerieReferencia(),
						entidad.getCorrelativoReferencia()),
				entidad.getCodigoMotivo(), entidad.getDescripcionMotivo(), lineas, totales, entidad.getEstadoSunat(),
				entidad.getObservacion(), entidad.isStockAplicado());
	}

	void copiar(Nota nota, NotaJpaEntity entidad) {
		entidad.setTipo(nota.tipo());
		entidad.setSerie(nota.serie());
		entidad.setCorrelativo(nota.correlativo());
		entidad.setFechaEmision(nota.fechaEmision());
		entidad.setHoraEmision(nota.horaEmision());
		entidad.setMoneda(nota.moneda());
		entidad.setTipoCambio(nota.tipoCambio());
		entidad.setClienteId(nota.clienteId());
		entidad.setClienteTipoDocumento(nota.clienteDocumento().tipo());
		entidad.setClienteNumeroDocumento(nota.clienteDocumento().numero());
		entidad.setClienteNombre(nota.clienteNombre());
		entidad.setClienteDireccion(nota.clienteDireccion());
		entidad.setTipoReferencia(nota.referencia().tipo());
		entidad.setSerieReferencia(nota.referencia().serie());
		entidad.setCorrelativoReferencia(nota.referencia().correlativo());
		entidad.setCodigoMotivo(nota.codigoMotivo());
		entidad.setDescripcionMotivo(nota.descripcionMotivo());
		entidad.setGravadas(nota.totales().gravadas());
		entidad.setGravadasIvap(nota.totales().gravadasIvap());
		entidad.setExoneradas(nota.totales().exoneradas());
		entidad.setInafectas(nota.totales().inafectas());
		entidad.setExportacion(nota.totales().exportacion());
		entidad.setGratuitas(nota.totales().gratuitas());
		entidad.setDescuentoGlobal(nota.totales().descuentoGlobal());
		entidad.setFactorDescuento(nota.totales().factorDescuentoGlobal());
		entidad.setIgv(nota.totales().igv());
		entidad.setIvap(nota.totales().ivap());
		entidad.setIgvGratuitas(nota.totales().igvGratuitas());
		entidad.setIcbper(nota.totales().icbper());
		entidad.setValorVenta(nota.totales().valorVenta());
		entidad.setTotal(nota.totales().total());
		entidad.setEstadoSunat(nota.estadoSunat());
		entidad.setObservacion(nota.observacion());
		entidad.setStockAplicado(nota.stockAplicado());
		entidad.getLineas().clear();
		List<LineaNotaJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaVenta linea : nota.lineas()) {
			lineas.add(aEntidad(linea, entidad, orden++));
		}
		entidad.getLineas().addAll(lineas);
	}

	private LineaVenta linea(LineaNotaJpaEntity entidad) {
		return new LineaVenta(entidad.getProductoId(), entidad.getCodigo(), entidad.getDescripcion(),
				entidad.getUnidadMedida(),
				new LineaCalculada(entidad.getCantidad(), entidad.getAfectacion(), entidad.getPrecioUnitario(),
						entidad.getValorUnitario(), entidad.getPorcentajeImpuesto(), entidad.getDescuento(),
						entidad.getValorVenta(), entidad.getImpuesto(), entidad.getIcbper(),
						entidad.getIcbperPorBolsa()));
	}

	private static LineaNotaJpaEntity aEntidad(LineaVenta linea, NotaJpaEntity nota, int orden) {
		LineaNotaJpaEntity entidad = new LineaNotaJpaEntity();
		entidad.setNota(nota);
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
