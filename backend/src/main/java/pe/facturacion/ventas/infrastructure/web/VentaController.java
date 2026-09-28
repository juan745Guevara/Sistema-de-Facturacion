package pe.facturacion.ventas.infrastructure.web;

import java.time.LocalDate;

import jakarta.validation.Valid;

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
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase;
import pe.facturacion.ventas.application.port.in.ConsultarVentasUseCase.Filtro;
import pe.facturacion.ventas.application.port.in.EmitirVentaUseCase;
import pe.facturacion.ventas.domain.model.Venta;
import pe.facturacion.ventas.infrastructure.web.VentasDtos.CalculoRequest;
import pe.facturacion.ventas.infrastructure.web.VentasDtos.CalculoResponse;
import pe.facturacion.ventas.infrastructure.web.VentasDtos.EmisionRequest;
import pe.facturacion.ventas.infrastructure.web.VentasDtos.VentaResponse;

@Tag(name = "Ventas")
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
class VentaController {

	private final EmitirVentaUseCase emitir;
	private final ConsultarVentasUseCase consultar;
	private final EnviarDocumentoUseCase enviarSunat;

	@Operation(summary = "Recalcula importes sin emitir. El frontend solo muestra este resultado.")
	@PostMapping("/previsualizar")
	CalculoResponse previsualizar(@Valid @RequestBody CalculoRequest request) {
		return CalculoResponse.desde(emitir.previsualizar(VentasDtos.aCalculo(request)));
	}

	@Operation(summary = "Emite una factura, boleta o nota de venta. El backend recalcula todos los importes.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	VentaResponse emitir(@Valid @RequestBody EmisionRequest request) {
		return VentaResponse.desde(emitir.emitir(VentasDtos.aEmision(request)));
	}

	@Operation(summary = "Busca ventas por tipo, fechas, cliente o número")
	@GetMapping
	Pagina<VentaResponse> buscar(@RequestParam(required = false) TipoComprobante tipo,
			@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
			@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return consultar.buscar(new Filtro(tipo, desde, hasta), new ConsultaPaginada(q, pagina, tamanio))
				.map(VentaResponse::desde);
	}

	@Operation(summary = "Obtiene una venta")
	@GetMapping("/{id}")
	VentaResponse obtener(@PathVariable Long id) {
		return VentaResponse.desde(consultar.obtener(id));
	}

	@Operation(summary = "Reenvía a SUNAT una factura o boleta que quedó pendiente")
	@PostMapping("/{id}/enviar")
	DocumentoElectronicoDto reenviar(@PathVariable Long id) {
		Venta venta = consultar.obtener(id);
		return enviarSunat.enviar(venta.tipo(), venta.serie(), venta.correlativo());
	}

}
