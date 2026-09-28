package pe.facturacion.notas.domain.model;

import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

/** Comprobante que modifica la nota (factura o boleta). */
public record ReferenciaComprobante(TipoComprobante tipo, String serie, int correlativo) {

	public ReferenciaComprobante {
		Objects.requireNonNull(tipo, "tipo");
		if (tipo != TipoComprobante.FACTURA && tipo != TipoComprobante.BOLETA) {
			throw DominioException.reglaNegocio("referencia-invalida",
					"La nota solo puede referenciar una factura o una boleta");
		}
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (!serie.matches("[A-Z0-9]{4}")) {
			throw DominioException.reglaNegocio("serie-invalida", "La serie de referencia debe tener 4 letras o dígitos");
		}
		if (correlativo < 1) {
			throw DominioException.reglaNegocio("correlativo-invalido", "El correlativo de referencia debe ser mayor que cero");
		}
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

}
