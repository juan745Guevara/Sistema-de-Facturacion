package pe.facturacion.sunat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("facturacion.sunat")
public record SunatProperties(
		Modo modo,
		String urlFacturacion,
		String usuarioSol,
		String claveSol,
		Certificado certificado) {

	public enum Modo {
		BETA,
		PRODUCCION
	}

	public record Certificado(String ruta, String clave) {
	}

}
