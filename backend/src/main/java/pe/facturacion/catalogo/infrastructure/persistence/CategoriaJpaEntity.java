package pe.facturacion.catalogo.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.facturacion.shared.infrastructure.persistence.EntidadAuditable;

@Entity
@Table(name = "categoria", schema = "catalogo")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaJpaEntity extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100, unique = true)
	private String nombre;

}
