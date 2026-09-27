package pe.facturacion.shared.infrastructure.persistence;

import java.util.Locale;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

/** Traduce entre la paginación de la capa de aplicación y la de Spring Data. */
public final class Paginas {

	private Paginas() {
	}

	public static Pageable solicitud(ConsultaPaginada consulta, Sort orden) {
		return PageRequest.of(consulta.pagina(), consulta.tamanio(), orden);
	}

	public static <E, T> Pagina<T> desde(Page<E> pagina, Function<? super E, ? extends T> conversion) {
		return new Pagina<>(pagina.getContent().stream().<T>map(conversion).toList(), pagina.getNumber(),
				pagina.getSize(), pagina.getTotalElements());
	}

	/** Patrón para {@code LIKE} sin distinguir mayúsculas; {@code null} si no hay texto. */
	public static String patronBusqueda(ConsultaPaginada consulta) {
		if (consulta.texto() == null) {
			return null;
		}
		String escapado = consulta.texto().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escapado + "%";
	}

}
