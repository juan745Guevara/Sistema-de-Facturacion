/**
 * Registro de compras a proveedores. No se envían a SUNAT.
 */
@ApplicationModule(
		displayName = "Compras",
		allowedDependencies = {
				"shared",
				"empresa :: api",
				"empresa :: modelo",
				"catalogo :: api",
				"catalogo :: modelo",
				"proveedores :: api",
				"proveedores :: modelo"
		})
package pe.facturacion.compras;

import org.springframework.modulith.ApplicationModule;
