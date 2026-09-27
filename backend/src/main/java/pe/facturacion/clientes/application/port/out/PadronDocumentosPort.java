package pe.facturacion.clientes.application.port.out;

import java.util.Optional;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

public interface PadronDocumentosPort {

	/** Vacío si el padrón no tiene el documento. Recibe solo DNI o RUC. */
	Optional<DatosPadron> consultar(DocumentoIdentidad documento);

}
