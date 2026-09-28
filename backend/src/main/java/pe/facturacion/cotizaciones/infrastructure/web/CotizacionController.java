package pe.facturacion.cotizaciones.infrastructure.web;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.cotizaciones.application.port.in.GestionarCotizacionesUseCase;
import pe.facturacion.cotizaciones.application.port.in.GestionarCotizacionesUseCase.Item;
import pe.facturacion.cotizaciones.application.port.in.GestionarCotizacionesUseCase.Solicitud;
import pe.facturacion.cotizaciones.domain.model.Cotizacion;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

@Tag(name = "Cotizaciones")
@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
class CotizacionController {

	private final GestionarCotizacionesUseCase cotizaciones;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Cotizacion emitir(@Valid @RequestBody EmisionRequest request) {
		return cotizaciones.emitir(new Solicitud(request.serie(), request.clienteId(),
				request.items().stream().map(i -> new Item(i.productoId(), i.cantidad(), i.precioUnitario())).toList(),
				request.observacion()));
	}

	@GetMapping("/{id}")
	Cotizacion obtener(@PathVariable Long id) {
		return cotizaciones.obtener(id);
	}

	@GetMapping
	Pagina<Cotizacion> buscar(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return cotizaciones.buscar(new ConsultaPaginada(q, pagina, tamanio));
	}

	record ItemRequest(@NotNull Long productoId, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
			@DecimalMin(value = "0", inclusive = false) BigDecimal precioUnitario) {
	}

	record EmisionRequest(@NotNull @Size(min = 4, max = 4) String serie, @NotNull Long clienteId,
			@NotEmpty List<@Valid ItemRequest> items, @Size(max = 500) String observacion) {
	}

}
