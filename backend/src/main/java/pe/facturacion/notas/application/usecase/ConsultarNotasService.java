package pe.facturacion.notas.application.usecase;

import pe.facturacion.notas.application.port.in.ConsultarNotasUseCase;
import pe.facturacion.notas.application.port.out.NotaRepositoryPort;
import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public class ConsultarNotasService implements ConsultarNotasUseCase {

	private final NotaRepositoryPort notas;

	public ConsultarNotasService(NotaRepositoryPort notas) {
		this.notas = notas;
	}

	@Override
	public Nota obtener(Long id) {
		return notas.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Nota", id));
	}

	@Override
	public Pagina<Nota> buscar(TipoComprobante tipo, ConsultaPaginada consulta) {
		return notas.buscar(tipo, consulta);
	}

}
