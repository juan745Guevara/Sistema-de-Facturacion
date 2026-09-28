package pe.facturacion.sunat.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

import pe.facturacion.shared.application.dto.ConsultaPaginada;
import pe.facturacion.shared.application.dto.Pagina;
import pe.facturacion.sunat.domain.model.LoteSunat;
import pe.facturacion.sunat.domain.model.TipoLote;

public interface LoteRepositoryPort {

	Optional<LoteSunat> buscarPorId(Long id);

	Pagina<LoteSunat> buscar(TipoLote tipo, ConsultaPaginada consulta);

	int siguienteCorrelativo(TipoLote tipo, LocalDate fechaGeneracion);

	LoteSunat guardar(LoteSunat lote);

}
