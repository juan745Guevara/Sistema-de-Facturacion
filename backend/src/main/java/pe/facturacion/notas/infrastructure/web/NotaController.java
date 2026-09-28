package pe.facturacion.notas.infrastructure.web;

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
import pe.facturacion.notas.application.port.in.ConsultarNotasUseCase;
import pe.facturacion.notas.application.port.in.EmitirNotaUseCase;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.notas.infrastructure.web.NotasDtos.CalculoRequest;
import pe.facturacion.notas.infrastructure.web.NotasDtos.CalculoResponse;
import pe.facturacion.notas.infrastructure.web.NotasDtos.EmisionRequest;
import pe.facturacion.notas.infrastructure.web.NotasDtos.NotaResponse;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;

@Tag(name = "Notas")
@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
class NotaController {

	private final EmitirNotaUseCase emitir;
	private final ConsultarNotasUseCase consultar;
	private final EnviarDocumentoUseCase enviarSunat;

	@Operation(summary = "Recalcula importes de una nota sin emitir")
	@PostMapping("/previsualizar")
	CalculoResponse previsualizar(@Valid @RequestBody CalculoRequest request) {
		return CalculoResponse.desde(emitir.previsualizar(NotasDtos.aCalculo(request)));
	}

	@Operation(summary = "Emite una nota de crédito o de débito. El backend recalcula los importes")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	NotaResponse emitir(@Valid @RequestBody EmisionRequest request) {
		return NotaResponse.desde(emitir.emitir(NotasDtos.aEmision(request)));
	}

	@Operation(summary = "Lista notas")
	@GetMapping
	Pagina<NotaResponse> buscar(@RequestParam(required = false) TipoComprobante tipo,
			@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanio) {
		return consultar.buscar(tipo, new ConsultaPaginada(q, pagina, tamanio)).map(NotaResponse::desde);
	}

	@Operation(summary = "Obtiene una nota")
	@GetMapping("/{id}")
	NotaResponse obtener(@PathVariable Long id) {
		return NotaResponse.desde(consultar.obtener(id));
	}

	@Operation(summary = "Reenvía a SUNAT una nota pendiente")
	@PostMapping("/{id}/enviar")
	DocumentoElectronicoDto reenviar(@PathVariable Long id) {
		Nota nota = consultar.obtener(id);
		return enviarSunat.enviar(nota.tipo(), nota.serie(), nota.correlativo());
	}

}
