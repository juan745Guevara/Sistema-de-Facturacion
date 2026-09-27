package pe.facturacion.empresa.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DireccionEmbeddable {

	@Column(nullable = false, length = 200)
	private String direccion;

	@Column(nullable = false, length = 6)
	private String ubigeo;

	@Column(nullable = false, length = 60)
	private String departamento;

	@Column(nullable = false, length = 60)
	private String provincia;

	@Column(nullable = false, length = 60)
	private String distrito;

	@Column(name = "codigo_pais", nullable = false, length = 2)
	private String codigoPais;

	@Column(name = "codigo_establecimiento", nullable = false, length = 4)
	private String codigoEstablecimiento;

}
