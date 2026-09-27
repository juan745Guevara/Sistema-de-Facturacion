package pe.facturacion.catalogo.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoAfectacionIgvConverter;

@Entity
@Table(name = "producto", schema = "catalogo")
@Getter
@Setter
@NoArgsConstructor
public class ProductoJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 30, unique = true)
	private String codigo;

	@Column(nullable = false, length = 500)
	private String descripcion;

	@Column(name = "categoria_id", nullable = false)
	private Long categoriaId;

	@Column(name = "unidad_medida", nullable = false, length = 5)
	private String unidadMedida;

	@Convert(converter = TipoAfectacionIgvConverter.class)
	@Column(name = "tipo_afectacion_igv", nullable = false, length = 2)
	private TipoAfectacionIgv tipoAfectacionIgv;

	@Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioVenta;

	@Column(name = "precio_compra", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioCompra;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal stock;

}
