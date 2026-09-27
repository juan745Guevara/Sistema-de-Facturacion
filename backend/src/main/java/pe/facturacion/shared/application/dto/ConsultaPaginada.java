package pe.facturacion.shared.application.dto;

/** Búsqueda por texto libre con paginación desde 0. */
public record ConsultaPaginada(String texto, int pagina, int tamanio) {

	public static final int TAMANIO_MAXIMO = 100;

	public ConsultaPaginada {
		texto = texto == null || texto.isBlank() ? null : texto.trim();
		pagina = Math.max(pagina, 0);
		tamanio = Math.clamp(tamanio, 1, TAMANIO_MAXIMO);
	}

}
