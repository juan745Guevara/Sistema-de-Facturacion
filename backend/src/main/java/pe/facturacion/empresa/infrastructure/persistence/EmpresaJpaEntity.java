package pe.facturacion.empresa.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;

/** Fila única: el sistema factura para un solo emisor. */
@Entity
@Table(name = "empresa", schema = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class EmpresaJpaEntity extends EntidadAuditable {

	static final short ID_UNICO = 1;

	@Id
	private Short id;

	@Column(nullable = false, length = 11)
	private String ruc;

	@Column(name = "razon_social", nullable = false, length = 200)
	private String razonSocial;

	@Column(name = "nombre_comercial", length = 200)
	private String nombreComercial;

	@Embedded
	private DireccionEmbeddable domicilioFiscal;

	@Column(length = 20)
	private String telefono;

	@Column(name = "correo_ventas", length = 120)
	private String correoVentas;

	@Column(name = "correo_soporte", length = 120)
	private String correoSoporte;

	@Column(name = "porcentaje_igv", nullable = false, precision = 5, scale = 2)
	private BigDecimal porcentajeIgv;

	@Column(name = "bienes_selva", nullable = false)
	private boolean bienesSelva;

	@Column(name = "servicios_selva", nullable = false)
	private boolean serviciosSelva;

}
