package pe.facturacion.notas.application.port.in;

import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface ConsultarNotasUseCase {

	Nota obtener(Long id);

	Pagina<Nota> buscar(TipoComprobante tipo, ConsultaPaginada consulta);

}
