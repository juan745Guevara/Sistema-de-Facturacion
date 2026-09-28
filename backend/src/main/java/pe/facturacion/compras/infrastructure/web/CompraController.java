package pe.facturacion.compras.infrastructure.web;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase;
import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase.Item;
import pe.facturacion.compras.application.port.in.RegistrarCompraUseCase.Solicitud;
import pe.facturacion.compras.domain.model.Compra;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

@Tag(name = "Compras")
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
class CompraController {

	private final RegistrarCompraUseCase compras;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registra una compra y aumenta el stock")
	Compra registrar(@Valid @RequestBody CompraRequest request) {
		return compras.registrar(new Solicitud(request.tipo(), request.serie(), request.correlativo(),
				request.fechaEmision(), request.proveedorId(),
				request.items().stream().map(i -> new Item(i.productoId(), i.cantidad(), i.precioUnitario())).toList(),
				request.observacion()));
	}

	@PostMapping("/{id}/anular")
	@Operation(summary = "Anula la compra y devuelve el stock")
	Compra anular(@PathVariable Long id) {
		return compras.anular(id);
	}

	@GetMapping("/{id}")
	Compra obtener(@PathVariable Long id) {
		return compras.obtener(id);
	}

	@GetMapping
	Pagina<Compra> buscar(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return compras.buscar(new ConsultaPaginada(q, pagina, tamanio));
	}

	record ItemRequest(@NotNull Long productoId, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
			@DecimalMin(value = "0", inclusive = false) BigDecimal precioUnitario) {
	}

	record CompraRequest(
			@NotNull TipoComprobante tipo,
			@NotNull @Size(min = 1, max = 20) String serie,
			@NotNull @Size(min = 1, max = 20) String correlativo,
			LocalDate fechaEmision,
			@NotNull Long proveedorId,
			@NotEmpty List<@Valid ItemRequest> items,
			@Size(max = 500) String observacion) {
	}

}
