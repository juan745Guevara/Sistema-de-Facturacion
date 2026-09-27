package pe.facturacion.shared.domain.exception;

import java.util.Objects;

/** Error de negocio. El {@link TipoError} decide cómo lo expone la capa web. */
public class DominioException extends RuntimeException {

	public enum TipoError {
		REGLA_NEGOCIO,
		NO_ENCONTRADO,
		CONFLICTO,
		NO_AUTENTICADO,
		NO_AUTORIZADO,
		SERVICIO_NO_DISPONIBLE
	}

	private final TipoError tipo;
	private final String codigo;

	public DominioException(TipoError tipo, String codigo, String mensaje) {
		super(mensaje);
		this.tipo = Objects.requireNonNull(tipo, "tipo");
		this.codigo = Objects.requireNonNull(codigo, "codigo");
	}

	public static DominioException reglaNegocio(String codigo, String mensaje) {
		return new DominioException(TipoError.REGLA_NEGOCIO, codigo, mensaje);
	}

	public static DominioException conflicto(String codigo, String mensaje) {
		return new DominioException(TipoError.CONFLICTO, codigo, mensaje);
	}

	public TipoError tipo() {
		return tipo;
	}

	public String codigo() {
		return codigo;
	}

}
