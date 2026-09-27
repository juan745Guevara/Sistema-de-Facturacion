package pe.facturacion.shared.infrastructure.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import pe.facturacion.shared.domain.exception.DominioException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String TIPO_BASE = "urn:facturacion:error:";

	@ExceptionHandler(DominioException.class)
	ProblemDetail manejarDominio(DominioException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(estadoPara(ex.tipo()), ex.getMessage());
		problema.setType(URI.create(TIPO_BASE + ex.codigo()));
		problema.setProperty("codigo", ex.codigo());
		return problema;
	}

	/** Respaldo de las validaciones de unicidad cuando dos peticiones compiten por el mismo dato. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail manejarIntegridad(DataIntegrityViolationException ex) {
		log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"La operación entra en conflicto con datos existentes");
		problema.setType(URI.create(TIPO_BASE + "conflicto-de-datos"));
		problema.setProperty("codigo", "conflicto-de-datos");
		return problema;
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
				"No tiene permisos para esta operación");
		problema.setType(URI.create(TIPO_BASE + "acceso-denegado"));
		return problema;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail manejarInesperado(Exception ex) {
		log.error("Error no controlado", ex);
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrió un error inesperado");
		problema.setType(URI.create(TIPO_BASE + "interno"));
		return problema;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> Map.of("campo", e.getField(), "mensaje", String.valueOf(e.getDefaultMessage())))
				.toList();
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos inválidos");
		problema.setType(URI.create(TIPO_BASE + "validacion"));
		problema.setProperty("errores", errores);
		return ResponseEntity.badRequest().body(problema);
	}

	private static HttpStatus estadoPara(DominioException.TipoError tipo) {
		return switch (tipo) {
			case REGLA_NEGOCIO -> HttpStatus.UNPROCESSABLE_CONTENT;
			case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
			case CONFLICTO -> HttpStatus.CONFLICT;
			case NO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
			case NO_AUTORIZADO -> HttpStatus.FORBIDDEN;
			case SERVICIO_NO_DISPONIBLE -> HttpStatus.SERVICE_UNAVAILABLE;
		};
	}

}
