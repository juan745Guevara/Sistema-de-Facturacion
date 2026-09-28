package pe.facturacion.cotizaciones.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
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

@Entity
@Table(name = "linea", schema = "cotizaciones")
@Getter
@Setter
@NoArgsConstructor
public class LineaCotizacionJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cotizacion_id", nullable = false)
	private CotizacionJpaEntity cotizacion;

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

	@Column(name = "precio_unitario", nullable = false, precision = 16, scale = 10)
	private BigDecimal precioUnitario;

	@Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorVenta;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal impuesto;

	@Column(nullable = false)
	private int orden;

}
