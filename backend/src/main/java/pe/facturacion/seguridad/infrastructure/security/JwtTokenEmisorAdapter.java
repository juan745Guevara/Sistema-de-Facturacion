package pe.facturacion.seguridad.infrastructure.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.facturacion.seguridad.application.port.out.TokenEmisorPort;
import pe.facturacion.seguridad.domain.model.Usuario;
import pe.facturacion.seguridad.infrastructure.config.JwtProperties;

@Component
@RequiredArgsConstructor
class JwtTokenEmisorAdapter implements TokenEmisorPort {

	private final JwtEncoder encoder;
	private final JwtProperties propiedades;
	private final Clock reloj;

	@Override
	public TokenEmitido emitir(Usuario usuario) {
		Instant ahora = reloj.instant();
		Instant expira = ahora.plus(propiedades.expiracion());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(propiedades.emisor())
				.issuedAt(ahora)
				.expiresAt(expira)
				.subject(usuario.username())
				.claim(ClaimsJwt.USUARIO_ID, usuario.id())
				.claim(ClaimsJwt.NOMBRE, usuario.nombre())
				.claim(ClaimsJwt.ROLES, List.of(usuario.rol().name()))
				.build();
		JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
		return new TokenEmitido(token, expira);
	}

}
