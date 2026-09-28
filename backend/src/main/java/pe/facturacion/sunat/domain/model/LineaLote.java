package pe.facturacion.sunat.domain.model;

import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

/** Ítem de un resumen (condición 1 o 3) o de una comunicación de baja (siempre 3). */
public record LineaLote(
		TipoComprobante tipo,
		String serie,
		int correlativo,
		int condicion,
		TipoDocumentoIdentidad clienteTipo,
		String clienteNumero,
		Moneda moneda,
		TotalesComprobante totales,
		String motivoBaja) {

	public static final int ADICION = 1;
	public static final int BAJA = 3;

	public LineaLote {
		Objects.requireNonNull(tipo, "tipo");
		serie = serie == null ? "" : serie.trim().toUpperCase();
		if (condicion != ADICION && condicion != BAJA) {
			throw DominioException.reglaNegocio("condicion-invalida", "La condición del lote debe ser 1 (adición) o 3 (baja)");
		}
	}

	public String numero() {
		return serie + "-" + correlativo;
	}

}
