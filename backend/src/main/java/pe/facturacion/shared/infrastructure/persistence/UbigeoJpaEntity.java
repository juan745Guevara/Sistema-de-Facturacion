package pe.facturacion.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubigeo", schema = "shared")
@Getter
@NoArgsConstructor
public class UbigeoJpaEntity {

	@Id
	@Column(length = 6)
	private String codigo;

	@Column(nullable = false, length = 60)
	private String departamento;

	@Column(nullable = false, length = 60)
	private String provincia;

	@Column(nullable = false, length = 60)
	private String distrito;

}
