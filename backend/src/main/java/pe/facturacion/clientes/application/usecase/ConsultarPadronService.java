package pe.facturacion.clientes.application.usecase;

import java.util.Objects;
import java.util.Optional;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.clientes.application.port.in.ConsultarPadronUseCase;
import pe.facturacion.clientes.application.port.out.PadronDocumentosPort;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;
import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

public class ConsultarPadronService implements ConsultarPadronUseCase {

	private final PadronDocumentosPort padron;

	public ConsultarPadronService(PadronDocumentosPort padron) {
		this.padron = Objects.requireNonNull(padron);
	}

	@Override
	public Optional<DatosPadron> consultar(DocumentoIdentidad documento) {
		if (documento.tipo() != TipoDocumentoIdentidad.DNI && documento.tipo() != TipoDocumentoIdentidad.RUC) {
			throw DominioException.reglaNegocio("consulta-no-soportada", "Solo se pueden consultar DNI y RUC");
		}
		return padron.consultar(documento);
	}

}
