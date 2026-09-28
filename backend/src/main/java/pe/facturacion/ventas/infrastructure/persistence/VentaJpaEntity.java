package pe.facturacion.ventas.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoComprobanteConverter;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;
import pe.facturacion.ventas.domain.model.FormaPago;

@Entity
@Table(name = "venta", schema = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class VentaJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = TipoComprobanteConverter.class)
	@Column(name = "tipo_comprobante", nullable = false, length = 2)
	private TipoComprobante tipo;

	@Column(nullable = false, length = 4)
	private String serie;

	@Column(nullable = false)
	private int correlativo;

	@Column(name = "fecha_emision", nullable = false)
	private LocalDate fechaEmision;

	@Column(name = "hora_emision", nullable = false)
	private LocalTime horaEmision;

	@Column(name = "fecha_vencimiento")
	private LocalDate fechaVencimiento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 3)
	private Moneda moneda;

	@Column(name = "tipo_cambio", nullable = false, precision = 8, scale = 3)
	private BigDecimal tipoCambio;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Convert(converter = TipoDocumentoIdentidadConverter.class)
	@Column(name = "cliente_tipo_doc", nullable = false, length = 1)
	private TipoDocumentoIdentidad clienteTipoDocumento;

	@Column(name = "cliente_numero_doc", nullable = false, length = 15)
	private String clienteNumeroDocumento;

	@Column(name = "cliente_nombre", nullable = false, length = 200)
	private String clienteNombre;

	@Column(name = "cliente_direccion", length = 200)
	private String clienteDireccion;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal gravadas;
	@Column(name = "gravadas_ivap", nullable = false, precision = 12, scale = 2)
	private BigDecimal gravadasIvap;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal exoneradas;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal inafectas;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal exportacion;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal gratuitas;
	@Column(name = "descuento_global", nullable = false, precision = 12, scale = 2)
	private BigDecimal descuentoGlobal;
	@Column(name = "factor_descuento", nullable = false, precision = 8, scale = 5)
	private BigDecimal factorDescuento;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal igv;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal ivap;
	@Column(name = "igv_gratuitas", nullable = false, precision = 12, scale = 2)
	private BigDecimal igvGratuitas;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal icbper;
	@Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorVenta;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Enumerated(EnumType.STRING)
	@Column(name = "forma_pago", nullable = false, length = 10)
	private FormaPago formaPago;

	@Column(name = "bienes_selva", nullable = false)
	private boolean bienesSelva;
	@Column(name = "servicios_selva", nullable = false)
	private boolean serviciosSelva;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_sunat", nullable = false, length = 20)
	private EstadoSunat estadoSunat;

	@Column(length = 500)
	private String observacion;

	@Column(name = "stock_devuelto", nullable = false)
	private boolean stockDevuelto;

	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaVentaJpaEntity> lineas = new ArrayList<>();

	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("numero")
	private List<CuotaVentaJpaEntity> cuotas = new ArrayList<>();

}
