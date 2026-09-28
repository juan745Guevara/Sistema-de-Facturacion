/**
 * Notas de crédito y de débito.
 */
@ApplicationModule(
		displayName = "Notas",
		allowedDependencies = {
				"shared",
				"empresa :: api",
				"empresa :: modelo",
				"catalogo :: api",
				"catalogo :: modelo",
				"clientes :: api",
				"clientes :: modelo",
				"ventas :: api",
				"ventas :: modelo",
				"sunat :: api",
				"sunat :: api-dto",
				"sunat :: eventos"
		})
package pe.facturacion.notas;

import org.springframework.modulith.ApplicationModule;
