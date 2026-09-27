package pe.facturacion.clientes.application.port.in;

import java.util.Optional;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

/** Consulta de RUC (SUNAT) y DNI (RENIEC) en un servicio externo, para autocompletar formularios. */
public interface ConsultarPadronUseCase {

	Optional<DatosPadron> consultar(DocumentoIdentidad documento);

}
