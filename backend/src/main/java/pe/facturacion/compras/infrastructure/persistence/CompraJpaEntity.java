package pe.facturacion.compras.infrastructure.persistence;

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
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoComprobanteConverter;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;

@Entity
@Table(name = "compra", schema = "compras")
@Getter
@Setter
@NoArgsConstructor
public class CompraJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = TipoComprobanteConverter.class)
	@Column(name = "tipo_comprobante", nullable = false, length = 2)
	private TipoComprobante tipo;

	@Column(nullable = false, length = 20)
	private String serie;

	@Column(nullable = false, length = 20)
	private String correlativo;

	@Column(name = "fecha_emision", nullable = false)
	private LocalDate fechaEmision;

	@Column(name = "proveedor_id")
	private Long proveedorId;

	@Convert(converter = TipoDocumentoIdentidadConverter.class)
	@Column(name = "proveedor_tipo_doc", nullable = false, length = 1)
	private TipoDocumentoIdentidad proveedorTipoDocumento;

	@Column(name = "proveedor_numero_doc", nullable = false, length = 15)
	private String proveedorNumeroDocumento;

	@Column(name = "proveedor_nombre", nullable = false, length = 200)
	private String proveedorNombre;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 3)
	private Moneda moneda;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal gravadas;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal igv;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(nullable = false)
	private boolean anulada;

	@Column(length = 500)
	private String observacion;

	@OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaCompraJpaEntity> lineas = new ArrayList<>();

}
