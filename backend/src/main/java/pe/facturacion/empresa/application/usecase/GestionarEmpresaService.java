package pe.facturacion.empresa.application.usecase;

import java.util.Objects;

import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.empresa.application.port.out.EmpresaRepositoryPort;
import pe.facturacion.empresa.domain.exception.EmpresaNoConfiguradaException;
import pe.facturacion.empresa.domain.model.Empresa;

public class GestionarEmpresaService implements GestionarEmpresaUseCase {

	private final EmpresaRepositoryPort empresas;

	public GestionarEmpresaService(EmpresaRepositoryPort empresas) {
		this.empresas = Objects.requireNonNull(empresas);
	}

	@Override
	public Empresa obtener() {
		return empresas.obtener().orElseThrow(EmpresaNoConfiguradaException::new);
	}

	@Override
	public Empresa guardar(Empresa empresa) {
		return empresas.guardar(Objects.requireNonNull(empresa, "empresa"));
	}

}
