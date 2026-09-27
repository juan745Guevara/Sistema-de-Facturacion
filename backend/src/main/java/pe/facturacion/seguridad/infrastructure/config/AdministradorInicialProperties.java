package pe.facturacion.seguridad.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("facturacion.seguridad.admin-inicial")
public record AdministradorInicialProperties(String nombre, String username, String password) {

	public boolean configurado() {
		return username != null && !username.isBlank() && password != null && !password.isBlank();
	}

}
