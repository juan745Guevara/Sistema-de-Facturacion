package pe.facturacion.guias.infrastructure.persistence;

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
@Table(name = "linea", schema = "guias")
@Getter
@Setter
@NoArgsConstructor
public class LineaGuiaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "guia_id", nullable = false)
	private GuiaJpaEntity guia;

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

	@Column(nullable = false)
	private int orden;

}
