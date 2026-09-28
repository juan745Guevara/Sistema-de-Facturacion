package pe.facturacion.notas.infrastructure.persistence;

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

@Entity
@Table(name = "nota", schema = "notas")
@Getter
@Setter
@NoArgsConstructor
public class NotaJpaEntity extends EntidadAuditable {

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

	@Convert(converter = TipoComprobanteConverter.class)
	@Column(name = "ref_tipo", nullable = false, length = 2)
	private TipoComprobante tipoReferencia;

	@Column(name = "ref_serie", nullable = false, length = 4)
	private String serieReferencia;

	@Column(name = "ref_correlativo", nullable = false)
	private int correlativoReferencia;

	@Column(name = "codigo_motivo", nullable = false, length = 2)
	private String codigoMotivo;

	@Column(name = "descripcion_motivo", nullable = false, length = 250)
	private String descripcionMotivo;

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
	@Column(name = "estado_sunat", nullable = false, length = 20)
	private EstadoSunat estadoSunat;

	@Column(length = 500)
	private String observacion;

	@Column(name = "stock_aplicado", nullable = false)
	private boolean stockAplicado;

	@OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaNotaJpaEntity> lineas = new ArrayList<>();

}
