package pe.facturacion.clientes.infrastructure.persistence;

import java.time.LocalDate;

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

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;
import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;
import pe.facturacion.shared.infrastructure.persistence.TipoDocumentoIdentidadConverter;

@Entity
@Table(name = "cliente", schema = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class ClienteJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = TipoDocumentoIdentidadConverter.class)
	@Column(name = "tipo_documento", nullable = false, length = 1)
	private TipoDocumentoIdentidad tipoDocumento;

	@Column(name = "numero_documento", nullable = false, length = 15)
	private String numeroDocumento;

	@Column(nullable = false, length = 200)
	private String nombre;

	@Column(length = 200)
	private String direccion;

	@Column(length = 120)
	private String email;

	@Column(length = 20)
	private String telefono;

	@Column(name = "fecha_nacimiento")
	private LocalDate fechaNacimiento;

}
