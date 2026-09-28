package pe.facturacion.ventas.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "cuota", schema = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class CuotaVentaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venta_id", nullable = false)
	private VentaJpaEntity venta;

	@Column(nullable = false)
	private int numero;

	@Column(name = "fecha_pago", nullable = false)
	private LocalDate fechaPago;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

}
