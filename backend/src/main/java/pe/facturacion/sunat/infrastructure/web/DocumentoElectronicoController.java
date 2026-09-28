package pe.facturacion.sunat.infrastructure.web;

import java.time.LocalDate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.dto.FiltroDocumentos;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase.Archivo;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;

@Tag(name = "SUNAT")
@RestController
@RequestMapping("/api/sunat/documentos")
@RequiredArgsConstructor
class DocumentoElectronicoController {

	private final ConsultarDocumentosUseCase consultar;
	private final EnviarDocumentoUseCase enviar;

	@Operation(summary = "Lista documentos electrónicos por tipo, estado o fechas")
	@GetMapping
	Pagina<DocumentoElectronicoDto> buscar(@RequestParam(required = false) TipoComprobante tipo,
			@RequestParam(required = false) EstadoSunat estado, @RequestParam(required = false) LocalDate desde,
			@RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
		return consultar.buscar(new FiltroDocumentos(tipo, estado, desde, hasta),
				new ConsultaPaginada(q, pagina, tamanio));
	}

	@Operation(summary = "Consulta un documento por tipo, serie y correlativo")
	@GetMapping("/{tipo}/{serie}/{correlativo}")
	DocumentoElectronicoDto obtener(@PathVariable TipoComprobante tipo, @PathVariable String serie,
			@PathVariable int correlativo) {
		return consultar.obtener(tipo, serie, correlativo);
	}

	@Operation(summary = "Reenvía un documento pendiente a SUNAT")
	@PostMapping("/{tipo}/{serie}/{correlativo}/enviar")
	DocumentoElectronicoDto reenviar(@PathVariable TipoComprobante tipo, @PathVariable String serie,
			@PathVariable int correlativo) {
		return enviar.enviar(tipo, serie, correlativo);
	}

	@Operation(summary = "Descarga el XML firmado")
	@GetMapping("/{tipo}/{serie}/{correlativo}/xml")
	ResponseEntity<byte[]> xml(@PathVariable TipoComprobante tipo, @PathVariable String serie,
			@PathVariable int correlativo) {
		return archivo(consultar.xml(tipo, serie, correlativo), MediaType.APPLICATION_XML);
	}

	@Operation(summary = "Descarga el CDR (zip) de SUNAT")
	@GetMapping("/{tipo}/{serie}/{correlativo}/cdr")
	ResponseEntity<byte[]> cdr(@PathVariable TipoComprobante tipo, @PathVariable String serie,
			@PathVariable int correlativo) {
		return archivo(consultar.cdr(tipo, serie, correlativo), MediaType.APPLICATION_OCTET_STREAM);
	}

	private static ResponseEntity<byte[]> archivo(Archivo archivo, MediaType tipo) {
		return ResponseEntity.ok()
				.contentType(tipo)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(archivo.nombre())
						.build()
						.toString())
				.body(archivo.contenido());
	}

}
