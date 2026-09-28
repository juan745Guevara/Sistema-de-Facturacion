/**
 * Reportes, dashboard y exportación PDF y Excel.
 */
@ApplicationModule(
		displayName = "Reportes",
		allowedDependencies = {
				"shared",
				"ventas :: api",
				"ventas :: modelo",
				"notas :: api",
				"notas :: modelo",
				"compras :: api",
				"compras :: modelo",
				"sunat :: api",
				"sunat :: api-dto"
		})
package pe.facturacion.reportes;

import org.springframework.modulith.ApplicationModule;
