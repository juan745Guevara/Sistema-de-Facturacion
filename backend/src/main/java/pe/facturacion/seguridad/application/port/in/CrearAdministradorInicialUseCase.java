package pe.facturacion.seguridad.application.port.in;

public interface CrearAdministradorInicialUseCase {

	/** Crea el administrador solo si todavía no existe ningún usuario. Devuelve si lo creó. */
	boolean crearSiNoHayUsuarios(DatosAdministrador datos);

	record DatosAdministrador(String nombre, String username, String password) {
	}

}
