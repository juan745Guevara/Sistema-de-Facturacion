package pe.facturacion.sunat.infrastructure.xml;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.sunat.application.port.out.GeneradorXmlLotePort;
import pe.facturacion.sunat.domain.model.Emisor;
import pe.facturacion.sunat.domain.model.LineaLote;
import pe.facturacion.sunat.domain.model.LoteSunat;
import pe.facturacion.sunat.domain.model.TipoLote;

@Component
public class GeneradorXmlLote implements GeneradorXmlLotePort {

	static final String NS_RESUMEN = "urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1";
	static final String NS_BAJA = "urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1";
	private static final String SUNAT = "PE:SUNAT";
	private static final String CATALOGO = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo";

	@Override
	public byte[] generar(LoteSunat lote, Emisor emisor) {
		boolean resumen = lote.tipo() == TipoLote.RESUMEN_DIARIO;
		Document documento = Xml.nuevo();
		Nodo raiz = Nodo.raiz(documento, resumen ? NS_RESUMEN : NS_BAJA,
				resumen ? "SummaryDocuments" : "VoidedDocuments");
		raiz.ext("UBLExtensions").ext("UBLExtension").ext("ExtensionContent");
		raiz.cbc("UBLVersionID", "2.0");
		raiz.cbc("CustomizationID", resumen ? "1.1" : "1.0");
		raiz.cbc("ID", lote.numero());
		raiz.cbc("ReferenceDate", lote.fechaReferencia().toString());
		raiz.cbc("IssueDate", lote.fechaGeneracion().toString());
		firma(raiz, emisor);
		proveedor(raiz, emisor);
		var lineas = lote.lineas();
		for (int i = 0; i < lineas.size(); i++) {
			if (resumen) {
				lineaResumen(raiz, i + 1, lineas.get(i));
			} else {
				lineaBaja(raiz, i + 1, lineas.get(i));
			}
		}
		return Xml.escribir(documento);
	}

	private static void firma(Nodo raiz, Emisor emisor) {
		Nodo firma = raiz.cac("Signature");
		firma.cbc("ID", emisor.ruc());
		Nodo firmante = firma.cac("SignatoryParty");
		firmante.cac("PartyIdentification").cbc("ID", emisor.ruc());
		firmante.cac("PartyName").cbc("Name", emisor.razonSocial());
		firma.cac("DigitalSignatureAttachment").cac("ExternalReference").cbc("URI", "#SignatureSP");
	}

	private static void proveedor(Nodo raiz, Emisor emisor) {
		Nodo parte = raiz.cac("AccountingSupplierParty").cac("Party");
		parte.cac("PartyIdentification").cbc("ID", emisor.ruc())
				.attr("schemeID", "6")
				.attr("schemeName", "Documento de Identidad")
				.attr("schemeAgencyName", SUNAT)
				.attr("schemeURI", CATALOGO + "06");
		parte.cac("PartyLegalEntity").cbc("RegistrationName", emisor.razonSocial());
	}

	private static void lineaResumen(Nodo raiz, int numero, LineaLote linea) {
		Nodo nodo = raiz.sac("SummaryDocumentsLine");
		nodo.cbc("LineID", numero);
		nodo.cbc("DocumentTypeCode", linea.tipo().codigo());
		nodo.cbc("ID", linea.numero());
		nodo.cac("AccountingCustomerParty").cac("Party").cac("PartyIdentification")
				.cbc("ID", linea.clienteNumero() == null ? "-" : linea.clienteNumero())
				.attr("schemeID", linea.clienteTipo() == null ? "0" : linea.clienteTipo().codigo());
		nodo.cac("Status").cbc("ConditionCode", Integer.toString(linea.condicion()));
		Moneda moneda = linea.moneda() == null ? Moneda.PEN : linea.moneda();
		TotalesComprobante t = linea.totales();
		BigDecimal total = t == null ? BigDecimal.ZERO : t.total();
		nodo.sac("TotalAmount").texto(Nodo.importe(total)).attr("currencyID", moneda.name());
		if (t != null) {
			pago(nodo, moneda, "01", t.gravadas());
			pago(nodo, moneda, "02", t.exoneradas());
			pago(nodo, moneda, "03", t.inafectas());
			pago(nodo, moneda, "04", t.exportacion());
			pago(nodo, moneda, "05", t.gratuitas());
			Nodo impuestos = nodo.cac("TaxTotal");
			impuestos.monto("TaxAmount", t.igv(), moneda);
			Nodo sub = impuestos.cac("TaxSubtotal");
			sub.monto("TaxAmount", t.igv(), moneda);
			Nodo esquema = sub.cac("TaxCategory").cac("TaxScheme");
			esquema.cbc("ID", "1000");
			esquema.cbc("Name", "IGV");
			esquema.cbc("TaxTypeCode", "VAT");
		}
	}

	private static void pago(Nodo linea, Moneda moneda, String codigo, BigDecimal monto) {
		if (monto == null || monto.signum() == 0) {
			return;
		}
		Nodo pago = linea.sac("BillingPayment");
		pago.cbc("PaidAmount", Nodo.importe(monto)).attr("currencyID", moneda.name());
		pago.cbc("InstructionID", codigo);
	}

	private static void lineaBaja(Nodo raiz, int numero, LineaLote linea) {
		Nodo nodo = raiz.sac("VoidedDocumentsLine");
		nodo.cbc("LineID", numero);
		nodo.cbc("DocumentTypeCode", linea.tipo().codigo());
		nodo.sac("DocumentSerialID").texto(linea.serie());
		nodo.sac("DocumentNumberID").texto(Integer.toString(linea.correlativo()));
		nodo.sac("VoidReasonDescription").texto(
				linea.motivoBaja() == null || linea.motivoBaja().isBlank() ? "ERROR EN EL DOCUMENTO" : linea.motivoBaja());
	}

}
