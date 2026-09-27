package pe.facturacion.catalogo.infrastructure.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.catalogo.application.port.in.GestionarProductosUseCase;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.ProductoRequest;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.ProductoResponse;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

@Tag(name = "Catálogo")
@RestController
@RequestMapping("/api/catalogo/productos")
@RequiredArgsConstructor
class ProductoController {

	private final GestionarProductosUseCase productos;
	private final CatalogoWebMapper mapper;

	@Operation(summary = "Busca productos por código o descripción")
	@GetMapping
	Pagina<ProductoResponse> buscar(@RequestParam(required = false) String q,
			@RequestParam(required = false) Long categoriaId,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return productos.buscar(new ConsultaPaginada(q, pagina, tamanio), categoriaId).map(mapper::aResponse);
	}

	@Operation(summary = "Obtiene un producto")
	@GetMapping("/{id}")
	ProductoResponse obtener(@PathVariable Long id) {
		return mapper.aResponse(productos.obtener(id));
	}

	@Operation(summary = "Registra un producto")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
		return mapper.aResponse(productos.crear(mapper.aDominio(request)));
	}

	@Operation(summary = "Actualiza un producto")
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
		return mapper.aResponse(productos.actualizar(id, mapper.aDominio(request)));
	}

	@Operation(summary = "Elimina un producto")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	void eliminar(@PathVariable Long id) {
		productos.eliminar(id);
	}

}
