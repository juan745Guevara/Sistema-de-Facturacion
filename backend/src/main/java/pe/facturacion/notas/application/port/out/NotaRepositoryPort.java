package pe.facturacion.notas.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

import pe.facturacion.notas.domain.model.Nota;
import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public interface NotaRepositoryPort {

	Optional<Nota> buscarPorId(Long id);

	Optional<Nota> buscar(TipoComprobante tipo, String serie, int correlativo);

	Pagina<Nota> buscar(TipoComprobante tipo, ConsultaPaginada consulta);

	BigDecimal totalAcreditado(TipoComprobante tipoReferencia, String serieReferencia, int correlativoReferencia);

	Nota guardar(Nota nota);

}
