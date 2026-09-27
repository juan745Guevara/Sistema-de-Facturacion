package pe.facturacion.empresa.application.port.out;

import java.util.Optional;

import pe.facturacion.empresa.domain.model.Empresa;

public interface EmpresaRepositoryPort {

	Optional<Empresa> obtener();

	Empresa guardar(Empresa empresa);

}
