package pe.facturacion.shared.infrastructure.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

@Tag(name = "Catálogos SUNAT")
@RestController
@RequestMapping("/api/catalogos-sunat")
class CatalogosSunatController {

	/** {@code valor} es lo que la API espera y devuelve; {@code codigo} es el del catálogo SUNAT. */
	record OpcionCatalogo(String valor, String codigo, String descripcion) {
	}

	@Operation(summary = "Catálogo 06: tipos de documento de identidad")
	@GetMapping("/tipos-documento-identidad")
	List<OpcionCatalogo> tiposDocumentoIdentidad() {
		return Arrays.stream(TipoDocumentoIdentidad.values())
				.map(t -> new OpcionCatalogo(t.name(), t.codigo(), t.descripcion()))
				.toList();
	}

	@Operation(summary = "Catálogo 07: tipos de afectación del IGV")
	@GetMapping("/tipos-afectacion-igv")
	List<OpcionCatalogo> tiposAfectacionIgv() {
		return Arrays.stream(TipoAfectacionIgv.values())
				.map(t -> new OpcionCatalogo(t.name(), t.codigo(), t.descripcion()))
				.toList();
	}

}
