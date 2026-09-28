package pe.facturacion.sunat.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.TipoComprobanteConverter;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;

@Entity
@Table(name = "lote_linea", schema = "sunat")
@Getter
@Setter
@NoArgsConstructor
public class LineaLoteJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lote_id", nullable = false)
	private LoteJpaEntity lote;

	@Convert(converter = TipoComprobanteConverter.class)
	@Column(name = "tipo_comprobante", nullable = false, length = 2)
	private TipoComprobante tipo;

	@Column(nullable = false, length = 4)
	private String serie;

	@Column(nullable = false)
	private int correlativo;

	@Column(nullable = false)
	private int condicion;

	@Convert(converter = TipoDocumentoIdentidadConverter.class)
	@Column(name = "cliente_tipo", length = 1)
	private TipoDocumentoIdentidad clienteTipo;

	@Column(name = "cliente_numero", length = 15)
	private String clienteNumero;

	@Enumerated(EnumType.STRING)
	@Column(length = 3)
	private Moneda moneda;

	@Column(precision = 12, scale = 2)
	private BigDecimal gravadas;
	@Column(name = "gravadas_ivap", precision = 12, scale = 2)
	private BigDecimal gravadasIvap;
	@Column(precision = 12, scale = 2)
	private BigDecimal exoneradas;
	@Column(precision = 12, scale = 2)
	private BigDecimal inafectas;
	@Column(precision = 12, scale = 2)
	private BigDecimal exportacion;
	@Column(precision = 12, scale = 2)
	private BigDecimal gratuitas;
	@Column(name = "descuento_global", precision = 12, scale = 2)
	private BigDecimal descuentoGlobal;
	@Column(name = "factor_descuento", precision = 8, scale = 5)
	private BigDecimal factorDescuento;
	@Column(precision = 12, scale = 2)
	private BigDecimal igv;
	@Column(precision = 12, scale = 2)
	private BigDecimal ivap;
	@Column(name = "igv_gratuitas", precision = 12, scale = 2)
	private BigDecimal igvGratuitas;
	@Column(precision = 12, scale = 2)
	private BigDecimal icbper;
	@Column(name = "valor_venta", precision = 12, scale = 2)
	private BigDecimal valorVenta;
	@Column(precision = 12, scale = 2)
	private BigDecimal total;

	@Column(name = "motivo_baja", length = 250)
	private String motivoBaja;

	@Column(nullable = false)
	private int orden;

}
