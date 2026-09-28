package pe.facturacion.reportes.infrastructure.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase.Fila;
import pe.facturacion.reportes.application.port.in.ConsultarReportesUseCase.Resumen;

@Component
public class ExportadorReportes {

	public byte[] excel(Resumen resumen) {
		try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			var hoja = libro.createSheet("Reporte");
			Row cabecera = hoja.createRow(0);
			String[] titulos = { "Tipo", "Número", "Fecha", "Tercero", "Total", "Estado" };
			for (int i = 0; i < titulos.length; i++) {
				cabecera.createCell(i).setCellValue(titulos[i]);
			}
			int fila = 1;
			for (Fila item : resumen.filas()) {
				Row r = hoja.createRow(fila++);
				r.createCell(0).setCellValue(item.tipo());
				r.createCell(1).setCellValue(item.numero());
				r.createCell(2).setCellValue(item.fecha().toString());
				r.createCell(3).setCellValue(item.tercero());
				r.createCell(4).setCellValue(item.total().doubleValue());
				r.createCell(5).setCellValue(item.estado());
			}
			libro.write(salida);
			return salida.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo generar el Excel", e);
		}
	}

	public byte[] pdf(String titulo, Resumen resumen) {
		try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			Document documento = new Document();
			PdfWriter.getInstance(documento, salida);
			documento.open();
			documento.add(new Paragraph(titulo));
			documento.add(new Paragraph("Del %s al %s · Total %s".formatted(resumen.desde(), resumen.hasta(),
					resumen.total())));
			PdfPTable tabla = new PdfPTable(5);
			tabla.addCell("Tipo");
			tabla.addCell("Número");
			tabla.addCell("Fecha");
			tabla.addCell("Tercero");
			tabla.addCell("Total");
			for (Fila item : resumen.filas()) {
				tabla.addCell(item.tipo());
				tabla.addCell(item.numero());
				tabla.addCell(item.fecha().toString());
				tabla.addCell(item.tercero());
				tabla.addCell(item.total().toPlainString());
			}
			documento.add(tabla);
			documento.close();
			return salida.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo generar el PDF", e);
		}
	}

}
