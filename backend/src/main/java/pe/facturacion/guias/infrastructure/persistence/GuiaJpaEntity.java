package pe.facturacion.guias.infrastructure.persistence;

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

import pe.facturacion.shared.domain.model.sunat.EstadoSunat;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;

@Entity
@Table(name = "guia", schema = "guias")
@Getter
@Setter
@NoArgsConstructor
public class GuiaJpaEntity extends EntidadAuditable {

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

	@Column(name = "motivo_traslado", nullable = false, length = 2)
	private String motivoTraslado;

	@Column(nullable = false, length = 2)
	private String modalidad;

	@Column(name = "fecha_traslado", nullable = false)
	private LocalDate fechaTraslado;

	@Column(name = "peso_total", nullable = false, precision = 12, scale = 3)
	private BigDecimal pesoTotal;

	@Column(nullable = false)
	private int bultos;

	@Column(name = "ubigeo_partida", nullable = false, length = 6)
	private String ubigeoPartida;

	@Column(name = "direccion_partida", nullable = false, length = 200)
	private String direccionPartida;

	@Column(name = "ubigeo_llegada", nullable = false, length = 6)
	private String ubigeoLlegada;

	@Column(name = "direccion_llegada", nullable = false, length = 200)
	private String direccionLlegada;

	@Column(name = "transportista_doc", length = 15)
	private String transportistaDocumento;

	@Column(name = "transportista_nombre", length = 200)
	private String transportistaNombre;

	@Column(length = 10)
	private String placa;

	@Column(length = 20)
	private String licencia;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_sunat", nullable = false, length = 20)
	private EstadoSunat estadoSunat;

	@Column(length = 80)
	private String ticket;

	@Column(length = 500)
	private String observacion;

	@OneToMany(mappedBy = "guia", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden")
	private List<LineaGuiaJpaEntity> lineas = new ArrayList<>();

}
