package pe.facturacion.sunat.infrastructure.xml;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pe.facturacion.shared.domain.model.Moneda;

/** Envoltorio mínimo para escribir UBL sin repetir namespaces en cada elemento. */
final class Nodo {

	static final String CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
	static final String CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
	static final String EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
	static final String SAC = "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1";
	static final String DS = "http://www.w3.org/2000/09/xmldsig#";

	private final Document documento;
	private final Element elemento;

	private Nodo(Document documento, Element elemento) {
		this.documento = documento;
		this.elemento = elemento;
	}

	/** Crea la raíz con los prefijos habituales de SUNAT declarados. */
	static Nodo raiz(Document documento, String namespace, String nombre) {
		Element raiz = documento.createElementNS(namespace, nombre);
		String xmlns = "http://www.w3.org/2000/xmlns/";
		raiz.setAttributeNS(xmlns, "xmlns:cac", CAC);
		raiz.setAttributeNS(xmlns, "xmlns:cbc", CBC);
		raiz.setAttributeNS(xmlns, "xmlns:ds", DS);
		raiz.setAttributeNS(xmlns, "xmlns:ext", EXT);
		if (!namespace.startsWith("urn:oasis")) {
			raiz.setAttributeNS(xmlns, "xmlns:sac", SAC);
		}
		documento.appendChild(raiz);
		return new Nodo(documento, raiz);
	}

	Nodo cac(String nombre) {
		return hijo(CAC, "cac:" + nombre);
	}

	Nodo sac(String nombre) {
		return hijo(SAC, "sac:" + nombre);
	}

	Nodo ext(String nombre) {
		return hijo(EXT, "ext:" + nombre);
	}

	Nodo cbc(String nombre, String texto) {
		Nodo hijo = hijo(CBC, "cbc:" + nombre);
		hijo.elemento.setTextContent(texto);
		return hijo;
	}

	Nodo cbc(String nombre, int valor) {
		return cbc(nombre, Integer.toString(valor));
	}

	Nodo monto(String nombre, BigDecimal monto, Moneda moneda) {
		return cbc(nombre, importe(monto)).attr("currencyID", moneda.name());
	}

	Nodo attr(String nombre, String valor) {
		elemento.setAttribute(nombre, valor);
		return this;
	}

	static String importe(BigDecimal monto) {
		return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	static String decimal(BigDecimal valor) {
		return valor.stripTrailingZeros().toPlainString();
	}

	private Nodo hijo(String namespace, String nombreCalificado) {
		Element hijo = documento.createElementNS(namespace, nombreCalificado);
		elemento.appendChild(hijo);
		return new Nodo(documento, hijo);
	}

}
