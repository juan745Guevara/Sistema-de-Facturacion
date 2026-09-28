/**
 * Guías de remisión remitente. El envío a SUNAT es REST OAuth, no el SOAP de facturas.
 */
@ApplicationModule(
		displayName = "Guías",
		allowedDependencies = {
				"shared",
				"empresa :: api",
				"empresa :: modelo",
				"catalogo :: api",
				"catalogo :: modelo",
				"clientes :: api",
				"clientes :: modelo"
		})
package pe.facturacion.guias;

import org.springframework.modulith.ApplicationModule;
