package pe.facturacion.shared.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;

/** Guarda el código del catálogo 07 ({@code 10}, {@code 20}...) en lugar del nombre del enum. */
@Converter
public class TipoAfectacionIgvConverter implements AttributeConverter<TipoAfectacionIgv, String> {

	@Override
	public String convertToDatabaseColumn(TipoAfectacionIgv tipo) {
		return tipo == null ? null : tipo.codigo();
	}

	@Override
	public TipoAfectacionIgv convertToEntityAttribute(String codigo) {
		return codigo == null ? null : TipoAfectacionIgv.desdeCodigo(codigo);
	}

}
