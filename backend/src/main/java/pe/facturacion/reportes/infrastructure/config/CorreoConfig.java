package pe.facturacion.reportes.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration(proxyBeanMethods = false)
class CorreoConfig {

	@Bean
	@ConditionalOnProperty(name = "SMTP_HOST")
	JavaMailSender javaMailSender(@Value("${SMTP_HOST}") String host, @Value("${SMTP_PORT:587}") int puerto,
			@Value("${SMTP_USERNAME:}") String usuario, @Value("${SMTP_PASSWORD:}") String clave) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setPort(puerto);
		if (!usuario.isBlank()) {
			sender.setUsername(usuario);
			sender.setPassword(clave);
		}
		return sender;
	}

}
