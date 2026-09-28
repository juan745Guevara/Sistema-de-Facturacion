package pe.facturacion.reportes.infrastructure.mail;

import java.time.LocalDate;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase.Resumen;
import pe.facturacion.reportes.infrastructure.export.ExportadorReportes;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;

@Component
public class EnviarReporteCorreo {

	private final ObjectProvider<JavaMailSender> correo;
	private final ConsultarReportesUseCase reportes;
	private final ExportadorReportes exportador;

	public EnviarReporteCorreo(ObjectProvider<JavaMailSender> correo, ConsultarReportesUseCase reportes,
			ExportadorReportes exportador) {
		this.correo = correo;
		this.reportes = reportes;
		this.exportador = exportador;
	}

	public void enviar(String destinatario, String tipo, LocalDate desde, LocalDate hasta) {
		JavaMailSender sender = correo.getIfAvailable();
		if (sender == null) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "correo-no-configurado",
					"Configure SMTP_HOST para enviar reportes por correo");
		}
		boolean ventas = !"compras".equalsIgnoreCase(tipo);
		Resumen resumen = ventas ? reportes.periodoVentas(desde, hasta) : reportes.periodoCompras(desde, hasta);
		String titulo = ventas ? "Reporte de ventas" : "Reporte de compras";
		try {
			MimeMessage mensaje = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
			helper.setTo(destinatario);
			helper.setSubject(titulo);
			helper.setText("Adjunto el reporte del %s al %s. Total %s.".formatted(desde, hasta, resumen.total()));
			helper.addAttachment(ventas ? "reporte-ventas.pdf" : "reporte-compras.pdf",
					() -> new java.io.ByteArrayInputStream(exportador.pdf(titulo, resumen)));
			sender.send(mensaje);
		} catch (Exception e) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "correo-fallido",
					"No se pudo enviar el correo: " + e.getMessage());
		}
	}

}
