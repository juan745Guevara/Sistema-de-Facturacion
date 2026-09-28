package pe.facturacion.sunat.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.sunat.domain.model.TipoLote;

@Entity
@Table(name = "lote", schema = "sunat")
@Getter
@Setter
@NoArgsConstructor
public class LoteJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoLote tipo;

	@Column(nullable = false, length = 8)
	private String serie;

	@Column(nullable = false)
	private int correlativo;

	@Column(name = "fecha_referencia", nullable = false)
	private LocalDate fechaReferencia;

	@Column(name = "fecha_generacion", nullable = false)
	private LocalDate fechaGeneracion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoSunat estado;

	@Column(length = 50)
	private String ticket;

	@Column(name = "codigo_respuesta", length = 10)
	private String codigoRespuesta;

	@Column(length = 1000)
	private String mensaje;

	@Column(length = 100)
	private String hash;

	@Column(name = "xml_firmado")
	private byte[] xmlFirmado;

	private byte[] cdr;

	@Column(nullable = false)
	private int intentos;

	@Column(name = "ultimo_envio")
	private Instant ultimoEnvio;

	@OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaLoteJpaEntity> lineas = new ArrayList<>();

}
