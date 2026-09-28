package pe.facturacion.empresa.application.usecase;

import java.util.List;

import pe.facturacion.empresa.application.port.in.GestionarSeriesUseCase;
import pe.facturacion.empresa.application.port.out.SerieRepositoryPort;
import pe.facturacion.empresa.domain.model.Serie;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

public class GestionarSeriesService implements GestionarSeriesUseCase {

	private final SerieRepositoryPort series;

	public GestionarSeriesService(SerieRepositoryPort series) {
		this.series = series;
	}

	@Override
	public List<Serie> listar(TipoComprobante tipo) {
		return series.listar(tipo);
	}

	@Override
	public Serie crear(TipoComprobante tipo, String serie, int ultimoCorrelativo) {
		Serie nueva = Serie.nueva(tipo, serie, ultimoCorrelativo);
		if (series.existe(tipo, nueva.serie())) {
			throw DominioException.conflicto("serie-duplicada",
					"Ya existe la serie %s para %s".formatted(nueva.serie(), tipo.descripcion()));
		}
		return series.guardar(nueva);
	}

	@Override
	public Serie cambiarActivacion(Long id, boolean activa) {
		Serie serie = series.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Serie", id));
		return series.guardar(serie.conActivacion(activa));
	}

	@Override
	public int siguienteCorrelativo(TipoComprobante tipo, String serie) {
		String codigo = serie == null ? "" : serie.trim().toUpperCase();
		Serie actual = series.bloquear(tipo, codigo).orElseThrow(() -> DominioException.reglaNegocio(
				"serie-inexistente", "No existe la serie %s para %s".formatted(codigo, tipo.descripcion())));
		if (!actual.activa()) {
			throw DominioException.reglaNegocio("serie-inactiva", "La serie %s está desactivada".formatted(codigo));
		}
		return series.guardar(actual.siguiente()).correlativo();
	}

}
