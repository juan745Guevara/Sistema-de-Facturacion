package pe.facturacion.shared.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

/** Guarda el código del catálogo 06 ({@code 1}, {@code 6}...) en lugar del nombre del enum. */
@Converter
public class TipoDocumentoIdentidadConverter implements AttributeConverter<TipoDocumentoIdentidad, String> {

	@Override
	public String convertToDatabaseColumn(TipoDocumentoIdentidad tipo) {
		return tipo == null ? null : tipo.codigo();
	}

	@Override
	public TipoDocumentoIdentidad convertToEntityAttribute(String codigo) {
		return codigo == null ? null : TipoDocumentoIdentidad.desdeCodigo(codigo);
	}

}
