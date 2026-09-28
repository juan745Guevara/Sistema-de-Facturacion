/**
 * Facturas, boletas, notas de venta, cuotas y cálculo de totales.
 */
@ApplicationModule(
		displayName = "Ventas",
		allowedDependencies = {
				"shared",
				"empresa :: api",
				"empresa :: modelo",
				"catalogo :: api",
				"catalogo :: modelo",
				"clientes :: api",
				"clientes :: modelo",
				"sunat :: api",
				"sunat :: api-dto",
				"sunat :: eventos"
		})
package pe.facturacion.ventas;

import org.springframework.modulith.ApplicationModule;
