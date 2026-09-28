package pe.facturacion.reportes.infrastructure.web;

import java.time.LocalDate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase.Dashboard;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase.Resumen;
import pe.facturacion.reportes.infrastructure.export.ExportadorReportes;
import pe.facturacion.reportes.infrastructure.mail.EnviarReporteCorreo;

@Tag(name = "Reportes")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
class ReportesController {

	private final ConsultarReportesUseCase reportes;
	private final ExportadorReportes exportador;
	private final EnviarReporteCorreo correo;

	@GetMapping("/dashboard")
	Dashboard dashboard() {
		return reportes.dashboard();
	}

	@GetMapping("/ventas")
	Resumen ventas(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return reportes.periodoVentas(desde, hasta);
	}

	@GetMapping("/compras")
	Resumen compras(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return reportes.periodoCompras(desde, hasta);
	}

	@GetMapping("/ventas.xlsx")
	ResponseEntity<byte[]> ventasExcel(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return archivo(exportador.excel(reportes.periodoVentas(desde, hasta)),
				"reporte-ventas.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@GetMapping("/compras.xlsx")
	ResponseEntity<byte[]> comprasExcel(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return archivo(exportador.excel(reportes.periodoCompras(desde, hasta)),
				"reporte-compras.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@GetMapping("/ventas.pdf")
	ResponseEntity<byte[]> ventasPdf(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return archivo(exportador.pdf("Reporte de ventas", reportes.periodoVentas(desde, hasta)),
				"reporte-ventas.pdf", MediaType.APPLICATION_PDF_VALUE);
	}

	@GetMapping("/compras.pdf")
	ResponseEntity<byte[]> comprasPdf(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
		return archivo(exportador.pdf("Reporte de compras", reportes.periodoCompras(desde, hasta)),
				"reporte-compras.pdf", MediaType.APPLICATION_PDF_VALUE);
	}

	@PostMapping("/correo")
	void enviarCorreo(@RequestParam String destinatario, @RequestParam String tipo, @RequestParam LocalDate desde,
			@RequestParam LocalDate hasta) {
		correo.enviar(destinatario, tipo, desde, hasta);
	}

	private static ResponseEntity<byte[]> archivo(byte[] cuerpo, String nombre, String tipo) {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(tipo))
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
				.body(cuerpo);
	}

}
