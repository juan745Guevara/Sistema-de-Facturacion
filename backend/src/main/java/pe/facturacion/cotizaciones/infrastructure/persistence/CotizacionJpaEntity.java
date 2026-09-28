package pe.facturacion.cotizaciones.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;

@Entity
@Table(name = "cotizacion", schema = "cotizaciones")
@Getter
@Setter
@NoArgsConstructor
public class CotizacionJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 4)
	private String serie;

	@Column(nullable = false)
	private int correlativo;

	@Column(name = "fecha_emision", nullable = false)
	private LocalDate fechaEmision;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Convert(converter = TipoDocumentoIdentidadConverter.class)
	@Column(name = "cliente_tipo_doc", nullable = false, length = 1)
	private TipoDocumentoIdentidad clienteTipoDocumento;

	@Column(name = "cliente_numero_doc", nullable = false, length = 15)
	private String clienteNumeroDocumento;

	@Column(name = "cliente_nombre", nullable = false, length = 200)
	private String clienteNombre;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 3)
	private Moneda moneda;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal gravadas;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal igv;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(length = 500)
	private String observacion;

	@OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaCotizacionJpaEntity> lineas = new ArrayList<>();

}
