package pe.facturacion.sunat.infrastructure.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Lectura y escritura DOM segura: sin DTD ni entidades externas (las respuestas de SUNAT también se parsean aquí).
 * La salida no se indenta, porque cualquier espacio agregado después de firmar invalida la firma.
 */
public final class Xml {

	private Xml() {
	}

	public static Document nuevo() {
		Document documento = constructor().newDocument();
		documento.setXmlStandalone(true);
		return documento;
	}

	public static Document leer(byte[] contenido) {
		try {
			Document documento = constructor().parse(new ByteArrayInputStream(contenido));
			documento.setXmlStandalone(true);
			return documento;
		} catch (SAXException | java.io.IOException e) {
			throw new IllegalArgumentException("XML mal formado: " + e.getMessage(), e);
		}
	}

	public static byte[] escribir(Document documento) {
		try {
			TransformerFactory fabrica = TransformerFactory.newInstance();
			fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformador = fabrica.newTransformer();
			transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformador.setOutputProperty(OutputKeys.INDENT, "no");
			ByteArrayOutputStream salida = new ByteArrayOutputStream();
			transformador.transform(new DOMSource(documento), new StreamResult(salida));
			return salida.toByteArray();
		} catch (TransformerException e) {
			throw new IllegalStateException("No se pudo serializar el XML", e);
		}
	}

	/** Texto del primer elemento con ese nombre local en cualquier namespace, o {@code null}. */
	public static String texto(Document documento, String nombreLocal) {
		NodeList nodos = documento.getElementsByTagNameNS("*", nombreLocal);
		return nodos.getLength() == 0 ? null : nodos.item(0).getTextContent().trim();
	}

	public static List<String> textos(Document documento, String nombreLocal) {
		NodeList nodos = documento.getElementsByTagNameNS("*", nombreLocal);
		List<String> textos = new ArrayList<>(nodos.getLength());
		for (int i = 0; i < nodos.getLength(); i++) {
			textos.add(nodos.item(i).getTextContent().trim());
		}
		return textos;
	}

	private static DocumentBuilder constructor() {
		try {
			DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
			fabrica.setNamespaceAware(true);
			fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			fabrica.setXIncludeAware(false);
			fabrica.setExpandEntityReferences(false);
			fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			return fabrica.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

}
