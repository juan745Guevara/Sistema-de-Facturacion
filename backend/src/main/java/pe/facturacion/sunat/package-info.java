/**
 * XML UBL 2.1, firma, envío, CDR, resumen diario, comunicación de baja, consultas y tipo de cambio.
 */
@ApplicationModule(
		displayName = "SUNAT",
		allowedDependencies = { "shared", "empresa :: api", "empresa :: modelo" })
package pe.facturacion.sunat;

import org.springframework.modulith.ApplicationModule;
