package pe.facturacion.shared.infrastructure.persistence;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.facturacion.shared.application.port.out.Transacciones;

@Component
class TransaccionesSpring implements Transacciones {

	private final TransactionTemplate plantilla;

	TransaccionesSpring(PlatformTransactionManager transacciones) {
		this.plantilla = new TransactionTemplate(transacciones);
	}

	@Override
	public <T> T ejecutar(Supplier<T> operacion) {
		return plantilla.execute(estado -> operacion.get());
	}

}
