package pe.facturacion.catalogo.domain.model;

import java.util.Locale;

import pe.facturacion.shared.domain.model.Textos;

/** El nombre se guarda en mayúsculas, como en el sistema anterior. */
public record Categoria(Long id, String nombre) {

	public Categoria {
		nombre = Textos.obligatorio(nombre, "nombre", 100).toUpperCase(Locale.ROOT);
	}

	public static Categoria nueva(String nombre) {
		return new Categoria(null, nombre);
	}

	public Categoria renombrar(String nuevoNombre) {
		return new Categoria(id, nuevoNombre);
	}

}
