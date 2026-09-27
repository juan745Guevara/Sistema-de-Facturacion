package pe.facturacion.seguridad.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.port.out.PasswordHasherPort;

@Component
@RequiredArgsConstructor
class BCryptPasswordHasherAdapter implements PasswordHasherPort {

	private final PasswordEncoder encoder;

	@Override
	public String hashear(String passwordPlano) {
		return encoder.encode(passwordPlano);
	}

	@Override
	public boolean coincide(String passwordPlano, String hash) {
		return encoder.matches(passwordPlano, hash);
	}

}
