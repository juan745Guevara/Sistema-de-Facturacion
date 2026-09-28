package pe.facturacion.sunat.infrastructure.xml;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import pe.facturacion.shared.domain.model.Moneda;
import pe.facturacion.shared.domain.model.calculo.LineaCalculada;
import pe.facturacion.shared.domain.model.calculo.TotalesComprobante;
import pe.facturacion.shared.domain.model.sunat.Icbper;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv;
import pe.facturacion.shared.domain.model.sunat.TipoAfectacionIgv.Tributo;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.shared.domain.service.MontoEnLetras;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico.Cuota;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico.Linea;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico.Receptor;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico.Referencia;
import pe.facturacion.sunat.application.port.out.GeneradorXmlPort;
import pe.facturacion.sunat.domain.model.Emisor;

/** Factura, boleta y notas en UBL 2.1 según las guías de elaboración de XML de SUNAT (versión 2.0). */
@Component
public class GeneradorXmlUbl implements GeneradorXmlPort {

	static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
	static final String NS_CREDIT_NOTE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
	static final String NS_DEBIT_NOTE = "urn:oasis:names:specification:ubl:schema:xsd:DebitNote-2";

	private static final String CATALOGO = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo";
	private static final String SUNAT = "PE:SUNAT";
	private static final String UNECE = "United Nations Economic Commission for Europe";
	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String MOTIVO_NC_AJUSTE_FECHAS = "13";

	private enum Formato {
		INVOICE(NS_INVOICE, "Invoice", "InvoiceLine", "InvoicedQuantity", "LegalMonetaryTotal"),
		CREDIT_NOTE(NS_CREDIT_NOTE, "CreditNote", "CreditNoteLine", "CreditedQuantity", "LegalMonetaryTotal"),
		DEBIT_NOTE(NS_DEBIT_NOTE, "DebitNote", "DebitNoteLine", "DebitedQuantity", "RequestedMonetaryTotal");

		final String namespace;
		final String raiz;
		final String linea;
		final String cantidad;
		final String totales;

		Formato(String namespace, String raiz, String linea, String cantidad, String totales) {
			this.namespace = namespace;
			this.raiz = raiz;
			this.linea = linea;
			this.cantidad = cantidad;
			this.totales = totales;
		}

		static Formato de(TipoComprobante tipo) {
			return switch (tipo) {
				case FACTURA, BOLETA -> INVOICE;
				case NOTA_CREDITO -> CREDIT_NOTE;
				case NOTA_DEBITO -> DEBIT_NOTE;
				default -> throw new IllegalArgumentException("Sin formato UBL de comprobante: " + tipo);
			};
		}
	}

	@Override
	public byte[] generar(ComprobanteElectronico comprobante, Emisor emisor) {
		Formato formato = Formato.de(comprobante.tipo());
		Document documento = Xml.nuevo();
		Nodo raiz = Nodo.raiz(documento, formato.namespace, formato.raiz);
		Moneda moneda = comprobante.moneda();

		raiz.ext("UBLExtensions").ext("UBLExtension").ext("ExtensionContent");
		raiz.cbc("UBLVersionID", "2.1");
		raiz.cbc("CustomizationID", "2.0").attr("schemeAgencyName", SUNAT);
		raiz.cbc("ID", comprobante.numero());
		raiz.cbc("IssueDate", comprobante.fechaEmision().toString());
		raiz.cbc("IssueTime", HORA.format(comprobante.horaEmision()));
		if (formato == Formato.INVOICE) {
			if (comprobante.fechaVencimiento() != null) {
				raiz.cbc("DueDate", comprobante.fechaVencimiento().toString());
			}
			raiz.cbc("InvoiceTypeCode", comprobante.tipo().codigo())
					.attr("listAgencyName", SUNAT)
					.attr("listName", "Tipo de Documento")
					.attr("listURI", CATALOGO + "01")
					.attr("listID", tipoOperacion(comprobante))
					.attr("name", "Tipo de Operacion");
		}
		leyendas(raiz, comprobante);
		raiz.cbc("DocumentCurrencyCode", moneda.name())
				.attr("listID", "ISO 4217 Alpha")
				.attr("listName", "Currency")
				.attr("listAgencyName", UNECE);
		if (formato == Formato.INVOICE) {
			raiz.cbc("LineCountNumeric", comprobante.lineas().size());
		} else {
			referencia(raiz, comprobante.tipo(), comprobante.referencia());
		}
		firma(raiz, emisor);
		proveedor(raiz, emisor);
		cliente(raiz, comprobante.receptor());
		if (llevaFormaPago(comprobante)) {
			formaPago(raiz, comprobante);
		}
		TotalesComprobante totales = comprobante.totales();
		if (formato == Formato.INVOICE && totales.descuentoGlobal().signum() > 0) {
			Nodo descuento = raiz.cac("AllowanceCharge");
			descuento.cbc("ChargeIndicator", "false");
			descuento.cbc("AllowanceChargeReasonCode", "02")
					.attr("listAgencyName", SUNAT)
					.attr("listName", "Cargo/descuento")
					.attr("listURI", CATALOGO + "53");
			descuento.cbc("MultiplierFactorNumeric", Nodo.decimal(totales.factorDescuentoGlobal()));
			descuento.monto("Amount", totales.descuentoGlobal(), moneda);
			descuento.monto("BaseAmount", totales.gravadasAntesDeDescuento(), moneda);
		}
		impuestos(raiz, totales, moneda);
		Nodo monetario = raiz.cac(formato.totales);
		monetario.monto("LineExtensionAmount", totales.valorVenta(), moneda);
		monetario.monto("TaxInclusiveAmount", totales.total(), moneda);
		monetario.monto("PayableAmount", totales.total(), moneda);

		List<Linea> lineas = comprobante.lineas();
		for (int i = 0; i < lineas.size(); i++) {
			linea(raiz, formato, i + 1, lineas.get(i), moneda);
		}
		return Xml.escribir(documento);
	}

	private static String tipoOperacion(ComprobanteElectronico comprobante) {
		boolean exportacion = comprobante.lineas().stream()
				.allMatch(l -> l.calculo().afectacion() == TipoAfectacionIgv.EXPORTACION);
		return exportacion ? "0200" : "0101";
	}

	private static void leyendas(Nodo raiz, ComprobanteElectronico comprobante) {
		TotalesComprobante totales = comprobante.totales();
		raiz.cbc("Note", MontoEnLetras.convertir(totales.total(), comprobante.moneda()))
				.attr("languageLocaleID", "1000");
		if (totales.total().signum() == 0 && totales.gratuitas().signum() > 0) {
			raiz.cbc("Note", "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE")
					.attr("languageLocaleID", "1002");
		}
		if (comprobante.bienesSelva()) {
			raiz.cbc("Note", "BIENES TRANSFERIDOS EN LA AMAZONÍA REGIÓN SELVA PARA SER CONSUMIDOS EN LA MISMA")
					.attr("languageLocaleID", "2001");
		}
		if (comprobante.serviciosSelva()) {
			raiz.cbc("Note", "SERVICIOS PRESTADOS EN LA AMAZONÍA REGIÓN SELVA PARA SER CONSUMIDOS EN LA MISMA")
					.attr("languageLocaleID", "2002");
		}
	}

	private static void referencia(Nodo raiz, TipoComprobante tipo, Referencia referencia) {
		boolean credito = tipo == TipoComprobante.NOTA_CREDITO;
		Nodo discrepancia = raiz.cac("DiscrepancyResponse");
		discrepancia.cbc("ReferenceID", referencia.numero());
		discrepancia.cbc("ResponseCode", referencia.codigoMotivo())
				.attr("listAgencyName", SUNAT)
				.attr("listName", credito ? "Tipo de nota de credito" : "Tipo de nota de debito")
				.attr("listURI", CATALOGO + (credito ? "09" : "10"));
		discrepancia.cbc("Description", referencia.descripcionMotivo());
		Nodo documento = raiz.cac("BillingReference").cac("InvoiceDocumentReference");
		documento.cbc("ID", referencia.numero());
		documento.cbc("DocumentTypeCode", referencia.tipo().codigo())
				.attr("listAgencyName", SUNAT)
				.attr("listName", "Tipo de Documento")
				.attr("listURI", CATALOGO + "01");
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
		identificacion(parte, "6", emisor.ruc());
		if (emisor.nombreComercial() != null) {
			parte.cac("PartyName").cbc("Name", emisor.nombreComercial());
		}
		Nodo legal = parte.cac("PartyLegalEntity");
		legal.cbc("RegistrationName", emisor.razonSocial());
		Nodo direccion = legal.cac("RegistrationAddress");
		direccion.cbc("ID", emisor.ubigeo()).attr("schemeName", "Ubigeos").attr("schemeAgencyName", "PE:INEI");
		direccion.cbc("AddressTypeCode", emisor.codigoEstablecimiento())
				.attr("listAgencyName", SUNAT)
				.attr("listName", "Establecimientos anexos");
		direccion.cbc("CityName", emisor.provincia());
		direccion.cbc("CountrySubentity", emisor.departamento());
		direccion.cbc("District", emisor.distrito());
		direccion.cac("AddressLine").cbc("Line", emisor.direccion());
		direccion.cac("Country").cbc("IdentificationCode", emisor.codigoPais())
				.attr("listID", "ISO 3166-1")
				.attr("listAgencyName", UNECE)
				.attr("listName", "Country");
	}

	private static void cliente(Nodo raiz, Receptor receptor) {
		Nodo parte = raiz.cac("AccountingCustomerParty").cac("Party");
		identificacion(parte, receptor.tipoDocumento().codigo(), receptor.numeroDocumento());
		Nodo legal = parte.cac("PartyLegalEntity");
		legal.cbc("RegistrationName", receptor.nombre());
		if (receptor.direccion() != null && !receptor.direccion().isBlank()) {
			legal.cac("RegistrationAddress").cac("AddressLine").cbc("Line", receptor.direccion());
		}
	}

	private static void identificacion(Nodo parte, String tipoDocumento, String numero) {
		parte.cac("PartyIdentification").cbc("ID", numero)
				.attr("schemeID", tipoDocumento)
				.attr("schemeName", "Documento de Identidad")
				.attr("schemeAgencyName", SUNAT)
				.attr("schemeURI", CATALOGO + "06");
	}

	/** Obligatoria en facturas desde 2021, y en la nota de crédito que corrige cuotas (motivo 13). */
	private static boolean llevaFormaPago(ComprobanteElectronico comprobante) {
		return comprobante.tipo() == TipoComprobante.FACTURA
				|| (comprobante.tipo() == TipoComprobante.NOTA_CREDITO
						&& MOTIVO_NC_AJUSTE_FECHAS.equals(comprobante.referencia().codigoMotivo()));
	}

	private static void formaPago(Nodo raiz, ComprobanteElectronico comprobante) {
		Moneda moneda = comprobante.moneda();
		Nodo terminos = raiz.cac("PaymentTerms");
		terminos.cbc("ID", "FormaPago");
		if (!comprobante.alCredito()) {
			terminos.cbc("PaymentMeansID", "Contado");
			return;
		}
		BigDecimal pendiente = comprobante.cuotas().stream().map(Cuota::monto).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		terminos.cbc("PaymentMeansID", "Credito");
		terminos.monto("Amount", pendiente, moneda);
		List<Cuota> cuotas = comprobante.cuotas();
		for (int i = 0; i < cuotas.size(); i++) {
			Nodo cuota = raiz.cac("PaymentTerms");
			cuota.cbc("ID", "FormaPago");
			cuota.cbc("PaymentMeansID", "Cuota%03d".formatted(i + 1));
			cuota.monto("Amount", cuotas.get(i).monto(), moneda);
			cuota.cbc("PaymentDueDate", cuotas.get(i).fechaPago().toString());
		}
	}

	private static void impuestos(Nodo raiz, TotalesComprobante totales, Moneda moneda) {
		Nodo total = raiz.cac("TaxTotal");
		total.monto("TaxAmount", totales.impuestos(), moneda);
		subtotal(total, Tributo.IGV, totales.gravadas(), totales.igv(), moneda);
		subtotal(total, Tributo.IVAP, totales.gravadasIvap(), totales.ivap(), moneda);
		subtotal(total, Tributo.EXONERADO, totales.exoneradas(), BigDecimal.ZERO, moneda);
		subtotal(total, Tributo.INAFECTO, totales.inafectas(), BigDecimal.ZERO, moneda);
		subtotal(total, Tributo.EXPORTACION, totales.exportacion(), BigDecimal.ZERO, moneda);
		subtotal(total, Tributo.GRATUITO, totales.gratuitas(), totales.igvGratuitas(), moneda);
		if (totales.icbper().signum() > 0) {
			Nodo icbper = total.cac("TaxSubtotal");
			icbper.monto("TaxAmount", totales.icbper(), moneda);
			esquemaIcbper(icbper.cac("TaxCategory"));
		}
	}

	private static void subtotal(Nodo total, Tributo tributo, BigDecimal base, BigDecimal impuesto, Moneda moneda) {
		if (base.signum() == 0) {
			return;
		}
		Nodo subtotal = total.cac("TaxSubtotal");
		subtotal.monto("TaxableAmount", base, moneda);
		subtotal.monto("TaxAmount", impuesto, moneda);
		esquema(subtotal.cac("TaxCategory"), tributo);
	}

	private static void linea(Nodo raiz, Formato formato, int numero, Linea linea, Moneda moneda) {
		LineaCalculada calculo = linea.calculo();
		boolean gratuito = calculo.afectacion().gratuito();
		Nodo nodo = raiz.cac(formato.linea);
		nodo.cbc("ID", numero);
		nodo.cbc(formato.cantidad, Nodo.decimal(calculo.cantidad()))
				.attr("unitCode", linea.unidadMedida())
				.attr("unitCodeListID", "UN/ECE rec 20")
				.attr("unitCodeListAgencyName", UNECE);
		nodo.monto("LineExtensionAmount", calculo.valorVenta(), moneda);

		Nodo precio = nodo.cac("PricingReference").cac("AlternativeConditionPrice");
		precio.cbc("PriceAmount", Nodo.decimal(calculo.precioUnitario())).attr("currencyID", moneda.name());
		precio.cbc("PriceTypeCode", gratuito ? "02" : "01")
				.attr("listName", "Tipo de Precio")
				.attr("listAgencyName", SUNAT)
				.attr("listURI", CATALOGO + "16");

		if (calculo.descuento().signum() > 0) {
			BigDecimal base = calculo.valorVenta().add(calculo.descuento());
			Nodo descuento = nodo.cac("AllowanceCharge");
			descuento.cbc("ChargeIndicator", "false");
			descuento.cbc("AllowanceChargeReasonCode", "00")
					.attr("listAgencyName", SUNAT)
					.attr("listName", "Cargo/descuento")
					.attr("listURI", CATALOGO + "53");
			descuento.cbc("MultiplierFactorNumeric",
					Nodo.decimal(calculo.descuento().divide(base, 5, RoundingMode.HALF_UP)));
			descuento.monto("Amount", calculo.descuento(), moneda);
			descuento.monto("BaseAmount", base, moneda);
		}

		Nodo impuestos = nodo.cac("TaxTotal");
		impuestos.monto("TaxAmount", calculo.impuesto().add(calculo.icbper()), moneda);
		Nodo subtotal = impuestos.cac("TaxSubtotal");
		subtotal.monto("TaxableAmount", calculo.valorVenta(), moneda);
		subtotal.monto("TaxAmount", calculo.impuesto(), moneda);
		Nodo categoria = subtotal.cac("TaxCategory");
		categoria.cbc("Percent", Nodo.decimal(calculo.porcentajeImpuesto()));
		categoria.cbc("TaxExemptionReasonCode", calculo.afectacion().codigo())
				.attr("listAgencyName", SUNAT)
				.attr("listName", "Afectacion del IGV")
				.attr("listURI", CATALOGO + "07");
		esquema(categoria, calculo.afectacion().tributo());
		if (calculo.conIcbper()) {
			Nodo icbper = impuestos.cac("TaxSubtotal");
			icbper.monto("TaxAmount", calculo.icbper(), moneda);
			icbper.cbc("BaseUnitMeasure", Nodo.decimal(calculo.cantidad())).attr("unitCode", "NIU");
			Nodo categoriaIcbper = icbper.cac("TaxCategory");
			categoriaIcbper.monto("PerUnitAmount", calculo.icbperPorBolsa(), moneda);
			esquemaIcbper(categoriaIcbper);
		}

		Nodo item = nodo.cac("Item");
		item.cbc("Description", linea.descripcion());
		if (linea.codigo() != null && !linea.codigo().isBlank()) {
			item.cac("SellersItemIdentification").cbc("ID", linea.codigo());
		}
		nodo.cac("Price").cbc("PriceAmount", gratuito ? "0.00" : Nodo.decimal(calculo.valorUnitario()))
				.attr("currencyID", moneda.name());
	}

	private static void esquema(Nodo categoria, Tributo tributo) {
		esquema(categoria, tributo.codigo(), tributo.nombre(), tributo.codigoInternacional());
	}

	private static void esquemaIcbper(Nodo categoria) {
		esquema(categoria, Icbper.CODIGO_TRIBUTO, "ICBPER", "OTH");
	}

	private static void esquema(Nodo categoria, String codigo, String nombre, String codigoInternacional) {
		Nodo esquema = categoria.cac("TaxScheme");
		esquema.cbc("ID", codigo)
				.attr("schemeName", "Codigo de tributos")
				.attr("schemeAgencyName", SUNAT)
				.attr("schemeURI", CATALOGO + "05");
		esquema.cbc("Name", nombre);
		esquema.cbc("TaxTypeCode", codigoInternacional);
	}

}
