package pe.facturacion.sunat.infrastructure.firma;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.shared.domain.exception.DominioException.TipoError;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort;
import pe.facturacion.sunat.infrastructure.config.SunatProperties.AlgoritmoFirma;
import pe.facturacion.sunat.infrastructure.xml.Xml;

/**
 * Firma XMLDSig envuelta (enveloped) dentro del último {@code ext:ExtensionContent}, con Id {@code SignatureSP}.
 * El almacén PKCS#12 se lee una sola vez, la primera vez que se firma.
 */
public class FirmaXmlDsig implements FirmaDigitalPort {

	private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
	private static final String ID_FIRMA = "SignatureSP";

	private final Path rutaCertificado;
	private final char[] clave;
	private final AlgoritmoFirma algoritmo;
	private volatile Credencial credencial;

	private record Credencial(PrivateKey llave, X509Certificate certificado) {
	}

	public FirmaXmlDsig(String rutaCertificado, String clave, AlgoritmoFirma algoritmo) {
		this.rutaCertificado = rutaCertificado == null || rutaCertificado.isBlank() ? null : Path.of(rutaCertificado);
		this.clave = clave == null ? new char[0] : clave.toCharArray();
		this.algoritmo = algoritmo == null ? AlgoritmoFirma.SHA256 : algoritmo;
	}

	@Override
	public XmlFirmado firmar(byte[] xml) {
		Credencial firmante = credencial();
		Document documento = Xml.leer(xml);
		NodeList extensiones = documento.getElementsByTagNameNS(NS_EXT, "ExtensionContent");
		if (extensiones.getLength() == 0) {
			throw new IllegalArgumentException("El XML no tiene ext:ExtensionContent donde colocar la firma");
		}
		try {
			XMLSignatureFactory fabrica = XMLSignatureFactory.getInstance("DOM");
			String digest = algoritmo == AlgoritmoFirma.SHA1 ? DigestMethod.SHA1 : DigestMethod.SHA256;
			String metodo = algoritmo == AlgoritmoFirma.SHA1 ? SignatureMethod.RSA_SHA1 : SignatureMethod.RSA_SHA256;
			Reference referencia = fabrica.newReference("", fabrica.newDigestMethod(digest, null),
					List.of(fabrica.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
			SignedInfo firmado = fabrica.newSignedInfo(
					fabrica.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
					fabrica.newSignatureMethod(metodo, null), List.of(referencia));
			KeyInfoFactory llaves = fabrica.getKeyInfoFactory();
			KeyInfo informacion = llaves.newKeyInfo(
					List.of(llaves.newX509Data(List.of(firmante.certificado()))));

			DOMSignContext contexto = new DOMSignContext(firmante.llave(),
					extensiones.item(extensiones.getLength() - 1));
			contexto.setDefaultNamespacePrefix("ds");
			fabrica.newXMLSignature(firmado, informacion, null, ID_FIRMA, null).sign(contexto);
		} catch (GeneralSecurityException | javax.xml.crypto.MarshalException
				| javax.xml.crypto.dsig.XMLSignatureException e) {
			throw new IllegalStateException("No se pudo firmar el XML", e);
		}
		String hash = documento.getElementsByTagNameNS(XMLSignature.XMLNS, "DigestValue").item(0).getTextContent();
		return new XmlFirmado(Xml.escribir(documento), hash);
	}

	private Credencial credencial() {
		Credencial actual = credencial;
		if (actual == null) {
			synchronized (this) {
				if (credencial == null) {
					credencial = cargar();
				}
				actual = credencial;
			}
		}
		return actual;
	}

	private Credencial cargar() {
		if (rutaCertificado == null) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "certificado-no-configurado",
					"No hay certificado digital configurado (CERT_PATH): no se puede firmar el comprobante");
		}
		try (InputStream entrada = Files.newInputStream(rutaCertificado)) {
			KeyStore almacen = KeyStore.getInstance("PKCS12");
			almacen.load(entrada, clave);
			for (String alias : Collections.list(almacen.aliases())) {
				if (almacen.isKeyEntry(alias)) {
					return new Credencial((PrivateKey) almacen.getKey(alias, clave),
							(X509Certificate) almacen.getCertificate(alias));
				}
			}
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "certificado-invalido",
					"El certificado digital no contiene una llave privada");
		} catch (IOException | GeneralSecurityException e) {
			throw new DominioException(TipoError.SERVICIO_NO_DISPONIBLE, "certificado-invalido",
					"No se pudo leer el certificado digital: revise CERT_PATH y CERT_PASSWORD");
		}
	}

}
