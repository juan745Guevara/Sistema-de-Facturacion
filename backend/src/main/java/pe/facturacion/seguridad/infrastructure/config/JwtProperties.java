package pe.facturacion.seguridad.infrastructure.config;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** HS256 exige una clave de al menos 256 bits, por eso el mínimo de 32 caracteres. */
@Validated
@ConfigurationProperties("facturacion.seguridad.jwt")
public record JwtProperties(
		@NotBlank @Size(min = 32, message = "JWT_SECRET debe tener al menos 32 caracteres") String secreto,
		@NotNull Duration expiracion,
		@NotBlank String emisor) {
}
