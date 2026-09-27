package pe.facturacion.shared.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

/** Documento de identidad validado según su tipo del catálogo 06. */
public record DocumentoIdentidad(TipoDocumentoIdentidad tipo, String numero) {

	private static final int[] PESOS_RUC = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
	private static final Set<String> PREFIJOS_RUC = Set.of("10", "15", "16", "17", "20");
	private static final Pattern ALFANUMERICO = Pattern.compile("[A-Z0-9-]{1,15}");
	private static final String SIN_NUMERO = "-";

	public DocumentoIdentidad {
		Objects.requireNonNull(tipo, "tipo");
		numero = numero == null ? "" : numero.trim().toUpperCase(Locale.ROOT);
		if (tipo == TipoDocumentoIdentidad.SIN_DOCUMENTO && numero.isEmpty()) {
			numero = SIN_NUMERO;
		}
		validar(tipo, numero);
	}

	public static DocumentoIdentidad ruc(String numero) {
		return new DocumentoIdentidad(TipoDocumentoIdentidad.RUC, numero);
	}

	public static DocumentoIdentidad dni(String numero) {
		return new DocumentoIdentidad(TipoDocumentoIdentidad.DNI, numero);
	}

	public boolean esRuc() {
		return tipo == TipoDocumentoIdentidad.RUC;
	}

	/** Prefijo válido y dígito verificador módulo 11 de SUNAT. */
	public static boolean esRucValido(String numero) {
		if (numero == null || !numero.matches("\\d{11}") || !PREFIJOS_RUC.contains(numero.substring(0, 2))) {
			return false;
		}
		int suma = 0;
		for (int i = 0; i < PESOS_RUC.length; i++) {
			suma += PESOS_RUC[i] * Character.digit(numero.charAt(i), 10);
		}
		int digito = (11 - suma % 11) % 10;
		return digito == Character.digit(numero.charAt(10), 10);
	}

	private static void validar(TipoDocumentoIdentidad tipo, String numero) {
		boolean valido = switch (tipo) {
			case RUC -> esRucValido(numero);
			case DNI -> numero.matches("\\d{8}");
			default -> ALFANUMERICO.matcher(numero).matches();
		};
		if (!valido) {
			throw new DominioException(TipoError.REGLA_NEGOCIO, "documento-invalido",
					"El número %s no es un %s válido".formatted(numero, tipo.descripcion()));
		}
	}

}
