package pe.facturacion.shared.application.port.out;

import java.util.function.Supplier;

/** Permite a los casos de uso, que no dependen de Spring, agrupar varias operaciones en una transacción. */
public interface Transacciones {

	<T> T ejecutar(Supplier<T> operacion);

	default void ejecutar(Runnable operacion) {
		ejecutar(() -> {
			operacion.run();
			return null;
		});
	}

}
