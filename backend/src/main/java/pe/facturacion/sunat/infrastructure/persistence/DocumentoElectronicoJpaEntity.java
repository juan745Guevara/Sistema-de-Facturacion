package pe.facturacion.sunat.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoComprobanteConverter;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;

@Entity
@Table(name = "documento_electronico", schema = "sunat")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoElectronicoJpaEntity extends EntidadAuditable {

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoSunat estado;

	@Column(name = "codigo_respuesta", length = 10)
	private String codigoRespuesta;

	@Column(length = 1000)
	private String mensaje;

	/** Una observación por línea. */
	@Column(length = 4000)
	private String observaciones;

	@Column(length = 100)
	private String hash;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private ComprobanteElectronico datos;

	@Column(name = "xml_firmado")
	private byte[] xmlFirmado;

	private byte[] cdr;

	@Column(nullable = false)
	private int intentos;

	@Column(name = "ultimo_envio")
	private Instant ultimoEnvio;

	@Version
	private long version;

}
