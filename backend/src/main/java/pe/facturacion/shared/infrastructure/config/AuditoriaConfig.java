package pe.facturacion.shared.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "auditorActual", dateTimeProviderRef = "proveedorFechaAuditoria")
public class AuditoriaConfig {

	public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
	private static final String AUDITOR_SISTEMA = "sistema";

	@Bean
	Clock reloj() {
		return Clock.system(ZONA_LIMA);
	}

	@Bean
	DateTimeProvider proveedorFechaAuditoria(Clock reloj) {
		return () -> Optional.of(reloj.instant());
	}

	@Bean
	AuditorAware<String> auditorActual() {
		return () -> {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
				return Optional.of(AUDITOR_SISTEMA);
			}
			return Optional.of(auth.getName());
		};
	}

}
