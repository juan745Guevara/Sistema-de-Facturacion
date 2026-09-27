package pe.facturacion.empresa.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.Textos;

/**
 * Emisor de los comprobantes. Las credenciales SOL, el certificado y el SMTP no viven aquí: se configuran por
 * variables de entorno.
 */
public record Empresa(
		String ruc,
		String razonSocial,
		String nombreComercial,
		Direccion domicilioFiscal,
		String telefono,
		String correoVentas,
		String correoSoporte,
		BigDecimal porcentajeIgv,
		boolean bienesSelva,
		boolean serviciosSelva) {

	private static final BigDecimal CIEN = BigDecimal.valueOf(100);

	public Empresa {
		ruc = DocumentoIdentidad.ruc(ruc).numero();
		razonSocial = Textos.obligatorio(razonSocial, "razón social", 200);
		nombreComercial = Textos.opcional(nombreComercial, "nombre comercial", 200);
		Objects.requireNonNull(domicilioFiscal, "domicilioFiscal");
		telefono = Textos.opcional(telefono, "teléfono", 20);
		correoVentas = Textos.opcional(correoVentas, "correo de ventas", 120);
		correoSoporte = Textos.opcional(correoSoporte, "correo de soporte", 120);
		porcentajeIgv = validarPorcentajeIgv(porcentajeIgv);
	}

	private static BigDecimal validarPorcentajeIgv(BigDecimal porcentaje) {
		if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(CIEN) >= 0
				|| porcentaje.stripTrailingZeros().scale() > 2) {
			throw DominioException.reglaNegocio("igv-invalido",
					"El porcentaje de IGV debe estar entre 0 y 99.99, con hasta 2 decimales");
		}
		return porcentaje.setScale(2, RoundingMode.UNNECESSARY);
	}

}
