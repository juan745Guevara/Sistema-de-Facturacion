package pe.facturacion.ventas.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.infrastructure.persistence.TipoAfectacionIgvConverter;

@Entity
@Table(name = "linea", schema = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class LineaVentaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venta_id", nullable = false)
	private VentaJpaEntity venta;

	@Column(name = "producto_id")
	private Long productoId;

	@Column(nullable = false, length = 30)
	private String codigo;

	@Column(nullable = false, length = 500)
	private String descripcion;

	@Column(name = "unidad_medida", nullable = false, length = 5)
	private String unidadMedida;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal cantidad;

	@Convert(converter = TipoAfectacionIgvConverter.class)
	@Column(name = "afectacion_igv", nullable = false, length = 2)
	private TipoAfectacionIgv afectacion;

	@Column(name = "precio_unitario", nullable = false, precision = 16, scale = 10)
	private BigDecimal precioUnitario;

	@Column(name = "valor_unitario", nullable = false, precision = 16, scale = 10)
	private BigDecimal valorUnitario;

	@Column(name = "porcentaje_impuesto", nullable = false, precision = 5, scale = 2)
	private BigDecimal porcentajeImpuesto;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal descuento;

	@Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorVenta;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal impuesto;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal icbper;

	@Column(name = "icbper_por_bolsa", nullable = false, precision = 12, scale = 2)
	private BigDecimal icbperPorBolsa;

	@Column(nullable = false)
	private int orden;

}
