package pe.facturacion.sunat.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

/**
 * Todo lo que hace falta para generar el XML UBL de una factura, boleta o nota. Los importes ya vienen
 * calculados y validados por el módulo que emite; aquí solo se representan.
 */
public record ComprobanteElectronico(
		TipoComprobante tipo,
		String serie,
		int correlativo,
		LocalDate fechaEmision,
		LocalTime horaEmision,
		LocalDate fechaVencimiento,
		Moneda moneda,
		Receptor receptor,
		List<Linea> lineas,
		TotalesComprobante totales,
		List<Cuota> cuotas,
		boolean bienesSelva,
		boolean serviciosSelva,
		Referencia referencia) {

	public ComprobanteElectronico {
		Objects.requireNonNull(tipo, "tipo");
		Objects.requireNonNull(serie, "serie");
		Objects.requireNonNull(fechaEmision, "fechaEmision");
		Objects.requireNonNull(horaEmision, "horaEmision");
		Objects.requireNonNull(moneda, "moneda");
		Objects.requireNonNull(receptor, "receptor");
		Objects.requireNonNull(totales, "totales");
		lineas = List.copyOf(lineas);
		cuotas = cuotas == null ? List.of() : List.copyOf(cuotas);
		if (!tipo.electronico() || tipo == TipoComprobante.GUIA_REMISION) {
			throw new IllegalArgumentException("No es un comprobante de pago electrónico: " + tipo);
		}
		if ((tipo == TipoComprobante.NOTA_CREDITO || tipo == TipoComprobante.NOTA_DEBITO) != (referencia != null)) {
			throw new IllegalArgumentException("Solo las notas llevan documento de referencia");
		}
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

	public boolean alCredito() {
		return !cuotas.isEmpty();
	}

	public record Receptor(TipoDocumentoIdentidad tipoDocumento, String numeroDocumento, String nombre,
			String direccion) {

		public Receptor {
			Objects.requireNonNull(tipoDocumento, "tipoDocumento");
			Objects.requireNonNull(numeroDocumento, "numeroDocumento");
			Objects.requireNonNull(nombre, "nombre");
		}
	}

	public record Linea(String codigo, String descripcion, String unidadMedida, LineaCalculada calculo) {

		public Linea {
			Objects.requireNonNull(descripcion, "descripcion");
			Objects.requireNonNull(unidadMedida, "unidadMedida");
			Objects.requireNonNull(calculo, "calculo");
		}
	}

	public record Cuota(BigDecimal monto, LocalDate fechaPago) {

		public Cuota {
			Objects.requireNonNull(monto, "monto");
			Objects.requireNonNull(fechaPago, "fechaPago");
		}
	}

	/** Comprobante que modifica una nota, con el motivo del catálogo 09 (crédito) o 10 (débito). */
	public record Referencia(TipoComprobante tipo, String serie, int correlativo, String codigoMotivo,
			String descripcionMotivo) {

		public Referencia {
			Objects.requireNonNull(tipo, "tipo");
			Objects.requireNonNull(serie, "serie");
			Objects.requireNonNull(codigoMotivo, "codigoMotivo");
			Objects.requireNonNull(descripcionMotivo, "descripcionMotivo");
		}

		public String numero() {
			return serie + "-" + correlativo;
		}
	}

}
