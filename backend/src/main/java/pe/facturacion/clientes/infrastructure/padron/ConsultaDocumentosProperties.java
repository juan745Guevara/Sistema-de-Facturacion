package pe.facturacion.clientes.infrastructure.padron;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Servicio externo de consulta RUC/DNI. Sin token, la consulta queda deshabilitada. */
@ConfigurationProperties("facturacion.consulta-documentos")
public record ConsultaDocumentosProperties(
		@DefaultValue("https://api.apifacturacion.com") URI url,
		String token,
		@DefaultValue("10s") Duration timeout) {
}
