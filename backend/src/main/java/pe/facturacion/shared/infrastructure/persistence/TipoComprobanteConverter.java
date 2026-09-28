package pe.facturacion.shared.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import pe.facturacion.shared.domain.model.sunat.TipoComprobante;

/** Guarda el código del catálogo 01 ({@code 01}, {@code 03}...) en lugar del nombre del enum. */
@Converter
public class TipoComprobanteConverter implements AttributeConverter<TipoComprobante, String> {

	@Override
	public String convertToDatabaseColumn(TipoComprobante tipo) {
		return tipo == null ? null : tipo.codigo();
	}

	@Override
	public TipoComprobante convertToEntityAttribute(String codigo) {
		return codigo == null ? null : TipoComprobante.desdeCodigo(codigo);
	}

}
