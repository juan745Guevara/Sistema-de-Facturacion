package pe.facturacion;

import java.util.concurrent.atomic.AtomicLong;

public final class DatosDePrueba {

	private static final int[] PESOS_RUC = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
	private static final AtomicLong SECUENCIA = new AtomicLong(System.nanoTime() % 10_000_000L);

	private DatosDePrueba() {
	}

	/** RUC de empresa (prefijo 20) con dígito verificador correcto y distinto en cada llamada. */
	public static String rucNuevo() {
		String base = "20" + "%08d".formatted(SECUENCIA.incrementAndGet() % 100_000_000L);
		return base + digitoVerificador(base);
	}

	/** DNI distinto en cada llamada. */
	public static String dniNuevo() {
		return "%08d".formatted(SECUENCIA.incrementAndGet() % 100_000_000L);
	}

	/** Sufijo único para códigos y nombres. */
	public static String unico() {
		return Long.toString(SECUENCIA.incrementAndGet(), 36).toUpperCase();
	}

	public static int digitoVerificador(String diezDigitos) {
		int suma = 0;
		for (int i = 0; i < PESOS_RUC.length; i++) {
			suma += PESOS_RUC[i] * Character.digit(diezDigitos.charAt(i), 10);
		}
		return (11 - suma % 11) % 10;
	}

}
