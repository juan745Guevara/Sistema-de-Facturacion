package pe.facturacion.sunat.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("facturacion.sunat")
public record SunatProperties(
		Modo modo,
		String urlFacturacion,
		String usuarioSol,
		String claveSol,
		Certificado certificado,
		@DefaultValue("60s") Duration timeout) {

	public enum Modo {
		BETA,
		PRODUCCION
	}

	public enum AlgoritmoFirma {
		SHA1,
		SHA256
	}

	/** SUNAT acepta RSA-SHA1 y RSA-SHA256; SHA256 es el valor por defecto. */
	public record Certificado(String ruta, String clave, @DefaultValue("SHA256") AlgoritmoFirma algoritmo) {
	}

}
