package pe.facturacion.catalogo.infrastructure.web;

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.catalogo.application.port.in.GestionarCategoriasUseCase;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.CategoriaRequest;
import pe.facturacion.catalogo.infrastructure.web.CatalogoDtos.CategoriaResponse;

@Tag(name = "Catálogo")
@RestController
@RequestMapping("/api/catalogo/categorias")
@RequiredArgsConstructor
class CategoriaController {

	private final GestionarCategoriasUseCase categorias;
	private final CatalogoWebMapper mapper;

	@Operation(summary = "Lista las categorías por nombre")
	@GetMapping
	List<CategoriaResponse> listar() {
		return categorias.listar().stream().map(mapper::aResponse).toList();
	}

	@Operation(summary = "Crea una categoría")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
		return mapper.aResponse(categorias.crear(request.nombre()));
	}

	@Operation(summary = "Renombra una categoría")
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	CategoriaResponse renombrar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return mapper.aResponse(categorias.renombrar(id, request.nombre()));
	}

	@Operation(summary = "Elimina una categoría sin productos")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESPECIAL')")
	void eliminar(@PathVariable Long id) {
		categorias.eliminar(id);
	}

}
