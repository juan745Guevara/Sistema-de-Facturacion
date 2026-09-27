package pe.facturacion.seguridad.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("facturacion.seguridad.cors")
public record CorsProperties(List<String> origenesPermitidos) {

	public CorsProperties {
		origenesPermitidos = origenesPermitidos == null ? List.of() : List.copyOf(origenesPermitidos);
	}

}
