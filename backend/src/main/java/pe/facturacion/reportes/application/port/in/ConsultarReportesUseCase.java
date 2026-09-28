package pe.facturacion.reportes.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ConsultarReportesUseCase {

	Resumen periodoVentas(LocalDate desde, LocalDate hasta);

	Resumen periodoCompras(LocalDate desde, LocalDate hasta);

	Dashboard dashboard();

	record Resumen(LocalDate desde, LocalDate hasta, long cantidad, BigDecimal gravadas, BigDecimal igv,
			BigDecimal total, List<Fila> filas) {
	}

	record Fila(String tipo, String numero, LocalDate fecha, String tercero, BigDecimal total, String estado) {
	}

	record Dashboard(BigDecimal ventasHoy, long comprobantesPendientes, long comprasDelMes, BigDecimal totalComprasMes) {
	}

}
