package pe.facturacion.guias.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import pe.facturacion.guias.application.port.in.GestionarGuiasUseCase;
import pe.facturacion.guias.application.port.in.GestionarGuiasUseCase.Item;
import pe.facturacion.guias.application.port.in.GestionarGuiasUseCase.Solicitud;
import pe.facturacion.guias.domain.model.Guia;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;

@Tag(name = "Guías")
@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
class GuiaController {

	private final GestionarGuiasUseCase guias;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Guia emitir(@Valid @RequestBody EmisionRequest request) {
		return guias.emitir(new Solicitud(request.serie(), request.clienteId(), request.motivoTraslado(),
				request.modalidad(), request.fechaTraslado(), request.pesoTotal(), request.bultos(),
				request.ubigeoPartida(), request.direccionPartida(), request.ubigeoLlegada(),
				request.direccionLlegada(), request.transportistaDocumento(), request.transportistaNombre(),
				request.placa(), request.licencia(),
				request.items().stream().map(i -> new Item(i.productoId(), i.cantidad())).toList(),
				request.observacion()));
	}

	@GetMapping("/{id}")
	Guia obtener(@PathVariable Long id) {
		return guias.obtener(id);
	}

	@GetMapping
	Pagina<Guia> buscar(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return guias.buscar(new ConsultaPaginada(q, pagina, tamanio));
	}

	record ItemRequest(@NotNull Long productoId, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad) {
	}

	record EmisionRequest(
			@NotNull @Size(min = 4, max = 4) String serie,
			@NotNull Long clienteId,
			@NotNull String motivoTraslado,
			@NotNull String modalidad,
			@NotNull LocalDate fechaTraslado,
			@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal pesoTotal,
			int bultos,
			@NotNull String ubigeoPartida,
			@NotNull String direccionPartida,
			@NotNull String ubigeoLlegada,
			@NotNull String direccionLlegada,
			String transportistaDocumento,
			String transportistaNombre,
			String placa,
			String licencia,
			@NotEmpty List<@Valid ItemRequest> items,
			@Size(max = 500) String observacion) {
	}

}
