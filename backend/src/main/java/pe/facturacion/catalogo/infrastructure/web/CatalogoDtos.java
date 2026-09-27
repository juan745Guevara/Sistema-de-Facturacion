package pe.facturacion.catalogo.infrastructure.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

final class CatalogoDtos {

	private CatalogoDtos() {
	}

	record CategoriaRequest(@NotBlank @Size(max = 100) String nombre) {
	}

	record CategoriaResponse(Long id, String nombre) {
	}

	record UnidadMedidaResponse(String codigo, String descripcion, boolean activa) {
	}

	record ActivacionRequest(@NotNull Boolean activa) {
	}

	record ProductoRequest(
			@NotBlank @Size(max = 30) String codigo,
			@NotBlank @Size(max = 500) String descripcion,
			@NotNull Long categoriaId,
			@NotBlank @Size(max = 5) String unidadMedida,
			@NotNull TipoAfectacionIgv tipoAfectacionIgv,
			@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2)
			BigDecimal precioVenta,
			@DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal precioCompra,
			@DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal stock) {
	}

	record ProductoResponse(
			Long id,
			String codigo,
			String descripcion,
			Long categoriaId,
			String unidadMedida,
			TipoAfectacionIgv tipoAfectacionIgv,
			BigDecimal precioVenta,
			BigDecimal precioCompra,
			BigDecimal stock) {
	}

}
