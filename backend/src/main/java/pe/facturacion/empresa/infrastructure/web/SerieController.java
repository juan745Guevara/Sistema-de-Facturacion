package pe.facturacion.empresa.infrastructure.web;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.domain.model.Serie;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

@Tag(name = "Series")
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
class SerieController {

	record SerieRequest(@NotNull TipoComprobante tipo, @NotBlank String serie,
			@NotNull @Min(0) @Max(99_999_999) Integer ultimoCorrelativo) {
	}

	record ActivacionRequest(@NotNull Boolean activa) {
	}

	record SerieResponse(Long id, TipoComprobante tipo, String serie, int correlativo, boolean activa) {

		static SerieResponse desde(Serie serie) {
			return new SerieResponse(serie.id(), serie.tipo(), serie.serie(), serie.correlativo(), serie.activa());
		}
	}

	private final GestionarSeriesUseCase series;

	@Operation(summary = "Series de numeración, opcionalmente de un tipo de comprobante")
	@GetMapping
	List<SerieResponse> listar(@RequestParam(required = false) TipoComprobante tipo) {
		return series.listar(tipo).stream().map(SerieResponse::desde).toList();
	}

	@Operation(summary = "Crea una serie, indicando el último número ya usado")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	SerieResponse crear(@Valid @RequestBody SerieRequest solicitud) {
		return SerieResponse.desde(series.crear(solicitud.tipo(), solicitud.serie(), solicitud.ultimoCorrelativo()));
	}

	@Operation(summary = "Activa o desactiva una serie")
	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	SerieResponse cambiarActivacion(@PathVariable Long id, @Valid @RequestBody ActivacionRequest solicitud) {
		return SerieResponse.desde(series.cambiarActivacion(id, solicitud.activa()));
	}

}
