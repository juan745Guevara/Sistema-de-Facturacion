package pe.facturacion.sunat.infrastructure.web;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.LoteDto;
import pe.facturacion.sunat.application.port.in.GestionarLotesUseCase;
import pe.facturacion.sunat.application.port.in.GestionarLotesUseCase.DocumentoPendiente;
import pe.facturacion.sunat.domain.model.TipoLote;

@Tag(name = "SUNAT lotes")
@RestController
@RequestMapping("/api/sunat/lotes")
@RequiredArgsConstructor
class LoteController {

	private final GestionarLotesUseCase lotes;

	@Operation(summary = "Boletas pendientes de un día, listas para el resumen")
	@GetMapping("/resumen/previsualizar")
	java.util.List<DocumentoPendiente> previsualizar(@RequestParam LocalDate fecha) {
		return lotes.previsualizarResumen(fecha);
	}

	@Operation(summary = "Genera y envía el resumen diario de boletas del día")
	@PostMapping("/resumen")
	@ResponseStatus(HttpStatus.CREATED)
	LoteDto generarResumen(@RequestParam LocalDate fecha) {
		return lotes.generarResumen(fecha);
	}

	@Operation(summary = "Comunica la baja de una factura o nota aceptada")
	@PostMapping("/baja")
	@ResponseStatus(HttpStatus.CREATED)
	LoteDto darBaja(@RequestBody BajaRequest request) {
		return lotes.darBaja(request.tipo(), request.serie(), request.correlativo(), request.motivo());
	}

	@Operation(summary = "Consulta el ticket de un resumen o baja")
	@PostMapping("/{id}/ticket")
	LoteDto consultarTicket(@PathVariable Long id) {
		return lotes.consultarTicket(id);
	}

	@Operation(summary = "Lista resúmenes y comunicaciones de baja")
	@GetMapping
	Pagina<LoteDto> buscar(@RequestParam(required = false) TipoLote tipo,
			@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
		return lotes.buscar(tipo, new ConsultaPaginada(null, pagina, tamanio));
	}

	@Operation(summary = "Obtiene un lote")
	@GetMapping("/{id}")
	LoteDto obtener(@PathVariable Long id) {
		return lotes.obtener(id);
	}

	record BajaRequest(@NotNull TipoComprobante tipo, @NotBlank String serie, @NotNull Integer correlativo,
			@NotBlank String motivo) {
	}

}
