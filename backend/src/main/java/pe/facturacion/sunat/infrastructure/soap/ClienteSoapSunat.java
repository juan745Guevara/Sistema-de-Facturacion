package pe.facturacion.sunat.infrastructure.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.sunat.application.port.out.ServicioSunatPort;
import pe.facturacion.sunat.domain.exception.ErrorComunicacionSunat;
import pe.facturacion.sunat.domain.model.RespuestaSunat;
import pe.facturacion.sunat.infrastructure.xml.Xml;

/**
 * Cliente del billService de SUNAT. El sobre SOAP se arma a mano (no hay WSDL que generar): son tres operaciones
 * con dos parámetros cada una, y la autenticación es un UsernameToken en texto plano sobre HTTPS.
 */
public class ClienteSoapSunat implements ServicioSunatPort {

	static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";
	static final String NS_SERVICIO = "http://service.sunat.gob.pe";
	static final String NS_WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

	private static final Pattern CODIGO_FALLA = Pattern.compile("(\\d{4})$");
	private static final Pattern SOLO_CODIGO = Pattern.compile("\\d{4}");
	private static final int FIN_EXCEPCIONES = 2000;

	/** Rechazo sin CDR: se transporta como excepción solo dentro de este adaptador. */
	private static final class RechazoSunat extends RuntimeException {

		private final transient RespuestaSunat respuesta;

		RechazoSunat(RespuestaSunat respuesta) {
			super(respuesta.descripcion(), null, false, false);
			this.respuesta = respuesta;
		}
	}

	private final RestClient cliente;
	private final String usuarioSol;
	private final String claveSol;

	public ClienteSoapSunat(RestClient cliente, String usuarioSol, String claveSol) {
		this.cliente = cliente;
		this.usuarioSol = usuarioSol;
		this.claveSol = claveSol;
	}

	@Override
	public RespuestaSunat enviarComprobante(String rucEmisor, String nombreArchivo, byte[] xmlFirmado) {
		Document respuesta;
		try {
			respuesta = invocar(rucEmisor, "sendBill", nombreArchivo + ".zip",
					Base64.getEncoder().encodeToString(comprimir(nombreArchivo + ".xml", xmlFirmado)));
		} catch (RechazoSunat rechazo) {
			return rechazo.respuesta;
		}
		String contenido = Xml.texto(respuesta, "applicationResponse");
		if (contenido == null) {
			throw new ErrorComunicacionSunat(null, "SUNAT respondió sin constancia de recepción (CDR)");
		}
		byte[] cdr = Base64.getMimeDecoder().decode(contenido);
		return leerCdr(cdr);
	}

	/** Lee el {@code R-*.xml} del zip devuelto por SUNAT. */
	static RespuestaSunat leerCdr(byte[] cdr) {
		Document constancia = Xml.leer(descomprimirXml(cdr));
		String codigo = Xml.texto(constancia, "ResponseCode");
		if (codigo == null) {
			throw new ErrorComunicacionSunat(null, "El CDR recibido no tiene código de respuesta");
		}
		String descripcion = Xml.texto(constancia, "Description");
		List<String> observaciones = Xml.textos(constancia, "Note");
		return new RespuestaSunat(codigo, descripcion, observaciones, cdr);
	}

	private Document invocar(String rucEmisor, String operacion, String nombreArchivo, String contenido) {
		if (usuarioSol == null || usuarioSol.isBlank() || claveSol == null || claveSol.isBlank()) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "credenciales-sol-no-configuradas",
					"No hay usuario y clave SOL configurados (SUNAT_USUARIO_SOL, SUNAT_CLAVE_SOL)");
		}
		byte[] sobre = sobre(rucEmisor + usuarioSol, claveSol, operacion, nombreArchivo, contenido);
		byte[] cuerpo;
		try {
			cuerpo = cliente.post()
					.contentType(new MediaType(MediaType.TEXT_XML, StandardCharsets.UTF_8))
					.header("SOAPAction", "urn:" + operacion)
					.body(sobre)
					.exchange((solicitud, respuesta) -> respuesta.getBody().readAllBytes());
		} catch (ResourceAccessException e) {
			throw new ErrorComunicacionSunat(null, "No se pudo conectar con SUNAT: " + e.getMostSpecificCause().getMessage());
		}
		Document respuesta;
		try {
			respuesta = Xml.leer(cuerpo);
		} catch (IllegalArgumentException e) {
			throw new ErrorComunicacionSunat(null, "SUNAT devolvió una respuesta que no es SOAP");
		}
		String falla = Xml.texto(respuesta, "faultcode");
		if (falla != null) {
			throw falla(falla, Xml.texto(respuesta, "faultstring"), Xml.texto(respuesta, "message"));
		}
		return respuesta;
	}

	/**
	 * SUNAT indica el código en el faultcode ({@code soap-env:Client.2800}) o, a veces, solo en el faultstring.
	 * Las excepciones (0100-1999) no invalidan el comprobante; los rechazos (2000-3999) sí.
	 */
	private static RuntimeException falla(String codigoFalla, String texto, String detalle) {
		String codigo = null;
		Matcher enCodigo = CODIGO_FALLA.matcher(codigoFalla);
		if (enCodigo.find()) {
			codigo = enCodigo.group(1);
		} else if (texto != null && SOLO_CODIGO.matcher(texto).matches()) {
			codigo = texto;
		}
		String mensaje = detalle != null && !detalle.isBlank() ? detalle : texto;
		if (codigo != null && Integer.parseInt(codigo) >= FIN_EXCEPCIONES) {
			return new RechazoSunat(RespuestaSunat.rechazada(codigo, mensaje));
		}
		return new ErrorComunicacionSunat(codigo, "SUNAT: " + (mensaje == null ? codigoFalla : mensaje));
	}

	static byte[] sobre(String usuario, String clave, String operacion, String nombreArchivo, String contenido) {
		Document documento = Xml.nuevo();
		Element sobre = documento.createElementNS(NS_SOAP, "soapenv:Envelope");
		sobre.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ser", NS_SERVICIO);
		sobre.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wsse", NS_WSSE);
		documento.appendChild(sobre);

		Element token = agregar(agregar(agregar(sobre, NS_SOAP, "soapenv:Header"), NS_WSSE, "wsse:Security"), NS_WSSE,
				"wsse:UsernameToken");
		agregar(token, NS_WSSE, "wsse:Username").setTextContent(usuario);
		agregar(token, NS_WSSE, "wsse:Password").setTextContent(clave);

		Element llamada = agregar(agregar(sobre, NS_SOAP, "soapenv:Body"), NS_SERVICIO, "ser:" + operacion);
		if (operacion.equals("getStatus")) {
			agregar(llamada, null, "ticket").setTextContent(contenido);
		} else {
			agregar(llamada, null, "fileName").setTextContent(nombreArchivo);
			agregar(llamada, null, "contentFile").setTextContent(contenido);
		}
		return Xml.escribir(documento);
	}

	private static Element agregar(Element padre, String namespace, String nombre) {
		Element hijo = padre.getOwnerDocument().createElementNS(namespace, nombre);
		padre.appendChild(hijo);
		return hijo;
	}

	static byte[] comprimir(String nombre, byte[] contenido) {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(salida)) {
			zip.putNextEntry(new ZipEntry(nombre));
			zip.write(contenido);
			zip.closeEntry();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return salida.toByteArray();
	}

	static byte[] descomprimirXml(byte[] zip) {
		try (ZipInputStream entrada = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry archivo = entrada.getNextEntry(); archivo != null; archivo = entrada.getNextEntry()) {
				if (!archivo.isDirectory() && archivo.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {
					return entrada.readAllBytes();
				}
			}
		} catch (IOException e) {
			throw new ErrorComunicacionSunat(null, "El CDR recibido no es un zip válido");
		}
		throw new ErrorComunicacionSunat(null, "El CDR recibido no contiene un XML");
	}

}
