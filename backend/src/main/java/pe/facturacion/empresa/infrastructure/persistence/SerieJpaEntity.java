package pe.facturacion.empresa.infrastructure.persistence;

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

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoComprobanteConverter;

@Entity
@Table(name = "serie", schema = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class SerieJpaEntity extends EntidadAuditable {

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

	@Column(nullable = false)
	private boolean activa;

}
