package pe.facturacion.shared.application.dto;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> contenido, int pagina, int tamanio, long totalElementos) {

	public Pagina {
		contenido = List.copyOf(contenido);
	}

	public <R> Pagina<R> map(Function<? super T, ? extends R> conversion) {
		return new Pagina<>(contenido.stream().<R>map(conversion).toList(), pagina, tamanio, totalElementos);
	}

}
