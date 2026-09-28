/**
 * Cotizaciones internas. No son comprobantes electrónicos.
 */
@ApplicationModule(
		displayName = "Cotizaciones",
		allowedDependencies = {
				"shared",
				"empresa :: api",
				"empresa :: modelo",
				"catalogo :: api",
				"catalogo :: modelo",
				"clientes :: api",
				"clientes :: modelo"
		})
package pe.facturacion.cotizaciones;

import org.springframework.modulith.ApplicationModule;
