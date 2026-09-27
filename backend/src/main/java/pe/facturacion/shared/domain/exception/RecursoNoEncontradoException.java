package pe.facturacion.shared.domain.exception;

public class RecursoNoEncontradoException extends DominioException {

	public RecursoNoEncontradoException(String recurso, Object id) {
		super(TipoError.NO_ENCONTRADO, "recurso-no-encontrado", "%s %s no existe".formatted(recurso, id));
	}

}
