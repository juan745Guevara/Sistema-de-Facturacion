package pe.facturacion.catalogo.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidad_medida", schema = "catalogo")
@Getter
@Setter
@NoArgsConstructor
public class UnidadMedidaJpaEntity {

	@Id
	@Column(length = 5)
	private String codigo;

	@Column(nullable = false, length = 60)
	private String descripcion;

	@Column(nullable = false)
	private boolean activa;

}
