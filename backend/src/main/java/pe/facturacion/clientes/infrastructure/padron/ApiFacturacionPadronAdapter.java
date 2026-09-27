package pe.facturacion.clientes.infrastructure.padron;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;

import pe.facturacion.clientes.application.dto.DatosPadron;
import pe.facturacion.clientes.application.port.out.PadronDocumentosPort;
import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.shared.domain.model.DocumentoIdentidad;

/** Cliente de api.apifacturacion.com, el mismo servicio que usaba el sistema PHP. */
class ApiFacturacionPadronAdapter implements PadronDocumentosPort {

	private static final Logger log = LoggerFactory.getLogger(ApiFacturacionPadronAdapter.class);

	record RespuestaRuc(
			String ruc,
			@JsonProperty("razon_social") String razonSocial,
			String estado,
			String condicion,
			String direccion,
			String ubigeo,
			String departamento,
			String provincia,
			String distrito) {
	}

	record RespuestaDni(String dni, String cliente) {
	}

	private final RestClient http;
	private final String token;

	ApiFacturacionPadronAdapter(RestClient http, String token) {
		this.http = http;
		this.token = token;
	}

	@Override
	public Optional<DatosPadron> consultar(DocumentoIdentidad documento) {
		if (token == null || token.isBlank()) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "consulta-no-configurada",
					"La consulta de documentos no está configurada");
		}
		try {
			return documento.esRuc() ? consultarRuc(documento) : consultarDni(documento);
		} catch (RestClientException ex) {
			log.warn("Falló la consulta de {} {}: {}", documento.tipo(), documento.numero(), ex.getMessage());
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "consulta-fallida",
					"El servicio de consulta de documentos no respondió");
		}
	}

	private Optional<DatosPadron> consultarRuc(DocumentoIdentidad documento) {
		RespuestaRuc r = pedir("/ruc/{numero}", documento, RespuestaRuc.class);
		if (r == null || r.ruc() == null || r.razonSocial() == null) {
			return Optional.empty();
		}
		return Optional.of(new DatosPadron(documento, r.razonSocial(), r.direccion(), r.ubigeo(), r.departamento(),
				r.provincia(), r.distrito(), r.estado(), r.condicion()));
	}

	private Optional<DatosPadron> consultarDni(DocumentoIdentidad documento) {
		RespuestaDni r = pedir("/dni/{numero}", documento, RespuestaDni.class);
		if (r == null || r.dni() == null || r.cliente() == null) {
			return Optional.empty();
		}
		return Optional.of(new DatosPadron(documento, r.cliente(), null, null, null, null, null, null, null));
	}

	private <T> T pedir(String ruta, DocumentoIdentidad documento, Class<T> tipo) {
		MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
		formulario.add("token", token);
		return http.post()
				.uri(ruta, documento.numero())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(formulario)
				.exchange((peticion, respuesta) -> {
					if (respuesta.getStatusCode().value() == 404) {
						return null;
					}
					if (respuesta.getStatusCode().isError()) {
						throw new RestClientException("HTTP " + respuesta.getStatusCode().value());
					}
					return respuesta.bodyTo(tipo);
				});
	}

}
