package pe.facturacion.sunat.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.sunat.domain.model.LineaLote;
import pe.facturacion.sunat.domain.model.LoteSunat;

@Component
class LotePersistenceMapper {

	LoteSunat aDominio(LoteJpaEntity entidad) {
		return new LoteSunat(entidad.getId(), entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(),
				entidad.getFechaReferencia(), entidad.getFechaGeneracion(), entidad.getEstado(), entidad.getTicket(),
				entidad.getCodigoRespuesta(), entidad.getMensaje(), entidad.getHash(), entidad.getXmlFirmado(),
				entidad.getCdr(), entidad.getIntentos(), entidad.getUltimoEnvio(),
				entidad.getLineas().stream().map(this::linea).toList());
	}

	void copiar(LoteSunat lote, LoteJpaEntity entidad) {
		entidad.setTipo(lote.tipo());
		entidad.setSerie(lote.serie());
		entidad.setCorrelativo(lote.correlativo());
		entidad.setFechaReferencia(lote.fechaReferencia());
		entidad.setFechaGeneracion(lote.fechaGeneracion());
		entidad.setEstado(lote.estado());
		entidad.setTicket(lote.ticket());
		entidad.setCodigoRespuesta(lote.codigoRespuesta());
		entidad.setMensaje(lote.mensaje());
		entidad.setHash(lote.hash());
		entidad.setXmlFirmado(lote.xmlFirmado());
		entidad.setCdr(lote.cdr());
		entidad.setIntentos(lote.intentos());
		entidad.setUltimoEnvio(lote.ultimoEnvio());
		entidad.getLineas().clear();
		List<LineaLoteJpaEntity> lineas = new ArrayList<>();
		int orden = 0;
		for (LineaLote linea : lote.lineas()) {
			lineas.add(aEntidad(linea, entidad, orden++));
		}
		entidad.getLineas().addAll(lineas);
	}

	private LineaLote linea(LineaLoteJpaEntity entidad) {
		TotalesComprobante totales = entidad.getTotal() == null ? null
				: new TotalesComprobante(n(entidad.getGravadas()), n(entidad.getGravadasIvap()), n(entidad.getExoneradas()),
						n(entidad.getInafectas()), n(entidad.getExportacion()), n(entidad.getGratuitas()),
						n(entidad.getDescuentoGlobal()), n(entidad.getFactorDescuento()), n(entidad.getIgv()),
						n(entidad.getIvap()), n(entidad.getIgvGratuitas()), n(entidad.getIcbper()),
						n(entidad.getValorVenta()), n(entidad.getTotal()));
		return new LineaLote(entidad.getTipo(), entidad.getSerie(), entidad.getCorrelativo(), entidad.getCondicion(),
				entidad.getClienteTipo(), entidad.getClienteNumero(), entidad.getMoneda(), totales,
				entidad.getMotivoBaja());
	}

	private static LineaLoteJpaEntity aEntidad(LineaLote linea, LoteJpaEntity lote, int orden) {
		LineaLoteJpaEntity entidad = new LineaLoteJpaEntity();
		entidad.setLote(lote);
		entidad.setTipo(linea.tipo());
		entidad.setSerie(linea.serie());
		entidad.setCorrelativo(linea.correlativo());
		entidad.setCondicion(linea.condicion());
		entidad.setClienteTipo(linea.clienteTipo());
		entidad.setClienteNumero(linea.clienteNumero());
		entidad.setMoneda(linea.moneda());
		TotalesComprobante t = linea.totales();
		if (t != null) {
			entidad.setGravadas(t.gravadas());
			entidad.setGravadasIvap(t.gravadasIvap());
			entidad.setExoneradas(t.exoneradas());
			entidad.setInafectas(t.inafectas());
			entidad.setExportacion(t.exportacion());
			entidad.setGratuitas(t.gratuitas());
			entidad.setDescuentoGlobal(t.descuentoGlobal());
			entidad.setFactorDescuento(t.factorDescuentoGlobal());
			entidad.setIgv(t.igv());
			entidad.setIvap(t.ivap());
			entidad.setIgvGratuitas(t.igvGratuitas());
			entidad.setIcbper(t.icbper());
			entidad.setValorVenta(t.valorVenta());
			entidad.setTotal(t.total());
		}
		entidad.setMotivoBaja(linea.motivoBaja());
		entidad.setOrden(orden);
		return entidad;
	}

	private static BigDecimal n(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

}
