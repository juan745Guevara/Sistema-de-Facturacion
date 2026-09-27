# Análisis del sistema antiguo (OPENFACT)

Referencia de solo lectura: `codigo-antiguo/`. No se modificó ese código.

Fuentes vigentes usadas: `ControladorVentas.php`, `ControladorEmpresa.php`, `ControladorEnvioSunat.php`, `ControladorResumenDiario.php`, `ControladorNotaCredito.php`, `ControladorNotaDebito.php`, `ControladorGuiaRemision.php`, `ControladorCompras.php`, `ControladorCotizaciones.php`, `ControladorClientes.php`, `ControladorProductos.php`, `ControladorUsuarios.php`, `ControladorReportes.php`, `api/GeneradorXML.php`, `api/ApiFacturacion.php`, `api/Signature.php`, `ajax/descuentos_items.ajax.php`, `ajax/redondeos.ajax.php`, `vistas/js/ventas.js`, `vistas/js/cuotas.js` y `BD/bdsistema.sql` (volcado phpMyAdmin del 2023-02-05, MariaDB 10.4.27).

Se ignoraron las copias de respaldo listadas en el plan de migración (`*BACKUP*`, `*ORI*`, `ventas.controlador2.php`, `cantidad_en_letrasO.php`, `panelO.php`, `* - copia.php`, vendors de terceros).

## 1. Qué hace el sistema

Aplicación web de facturación electrónica peruana para un emisor. Un usuario autenticado arma un carrito en sesión, calcula operaciones gravadas, exoneradas, inafectas y gratuitas, emite comprobantes, firma XML UBL 2.1 con un certificado `.pfx`, lo envía a SUNAT (beta o producción según `emisor.modo`) y guarda el CDR. También registra compras, cotizaciones y guías, e imprime PDF A4 o ticket.

Arquitectura: MVC casero (`Controladores`, `Modelos`, `vistas`), PDO en `Conect/Conexion.php`, rutas por `?ruta=` en `vistas/plantilla.php`, carrito en `$_SESSION`, respuestas AJAX que mezclan HTML y JavaScript. Zona horaria `America/Lima`. El emisor operativo está fijo en `idemisor = 1`.

## 2. Módulos y casos de uso

| Módulo nuevo | Código vigente | Casos de uso |
| --- | --- | --- |
| `seguridad` | `ControladorUsuarios`, `usuarios` | Login, alta, edición y baja de usuarios. El perfil es texto libre (`Administrador` en el volcado). No hay autorización por ruta. |
| `empresa` | `ControladorEmpresa`, `emisor` | Datos del emisor, logo, plantilla, modo beta/producción, IGV, bienes y servicios Selva, SMTP, certificado y credenciales SOL. |
| `catalogo` | `ControladorProductos`, `ControladorCategorias` | CRUD de productos, categorías y activación de unidades SUNAT. Precio de compra sugerido = 70 % del precio de venta. |
| `clientes` | `ControladorClientes`, `ModeloClientes` | CRUD. Consulta RUC (tipo 6) y DNI (tipo 1) contra `api.apifacturacion.com`. Cliente genérico `DNI 00000000`. |
| `proveedores` | `ControladorProveedores` | Alta y búsqueda. Misma forma que clientes, sin consulta RUC activa (el método está comentado). |
| `ventas` | `ControladorVentas`, `ventas.js`, `cuotas.js` | Emitir factura `01`, boleta `03` y nota de venta `02`. Carrito, descuentos, ICBPER, crédito en cuotas, stock y PDF. |
| `notas` | `ControladorNotaCredito`, `ControladorNotaDebito` | Nota de crédito `07` y débito `08` sobre un comprobante, con motivo del catálogo 09/10. |
| `guias` | `ControladorGuiaRemision`, `GeneradorXML::CrearXMLGuiaRemision` | Guía `09`: motivo, modalidad, peso, transportista, conductor, ubigeo de partida y llegada. Envío por API REST y ticket. |
| `compras` | `ControladorCompras` | Registrar compra de proveedor (no se envía a SUNAT). Anular compra. |
| `cotizaciones` | `ControladorCotizaciones` | Cotización tipo `00`, serie propia. No es comprobante electrónico. |
| `sunat` | `GeneradorXML`, `Signature`, `ApiFacturacion`, `ControladorEnvioSunat`, `ControladorResumenDiario` | XML, firma, envío, CDR, reenvío, resumen diario `RC`, comunicación de baja `RA`, consulta de comprobante, tipo de cambio. |
| `reportes` | `ControladorReportes` | Sumas de facturas, compras y notas; Excel y PDF; dashboard y más vendidos. |
| `shared` | `cantidad_en_letras.php`, catálogos SQL | Monto en letras, tipos de documento, afectación, medios de pago, ubigeo. |

Pantallas (`vistas/modulos/`, enrutadas en `plantilla.php`): inicio, usuarios, categorías, productos, unidad de medida, clientes, crear factura, crear boleta, crear nota de venta, nota de crédito, nota de débito, cotización, listar cotizaciones, guía, ver guías, nueva compra, ventas (estados SUNAT), resumen diario, reportes de ventas y compras, consulta de comprobante, empresa.

## 3. Reglas de negocio

### IGV

El porcentaje sale de `emisor.igv` (en el volcado, 18). No está hardcodeado en el cálculo principal.

- `igv_uno = (igv / 100) + 1` → con 18 % es `1.18`. Sirve para sacar el valor unitario desde el precio que ya incluye IGV.
- `igv_dos = igv / 100` → `0.18`.

El precio de catálogo se trata como precio con IGV. Valor unitario gravado = `precio_unitario / igv_uno`. IGV de línea = base gravada × `igv_dos`.

Por código de afectación (catálogo 07, tabla `tipo_afectacion`):

| Código | Tratamiento en el carrito y al guardar |
| --- | --- |
| `10` Gravado oneroso | Base = valor × cantidad − descuento de ítem. IGV sobre esa base. Entra a `op_gravadas`. |
| `11`–`16` Gravado gratuito | Base informativa × cantidad. Precio de venta 0, tipo de precio `02`. El IGV de la operación gratuita se acumula en `igv_op`, no en el IGV cobrado. |
| `20` Exonerado | Base = precio × cantidad − descuento. IGV 0. Entra a `op_exoneradas`. |
| `30` Inafecto | Igual que exonerado, en `op_inafectas`. |
| `31`–`36` Inafecto gratuito | Suma a gratuitas. IGV 0. |
| `40` Exportación | Existe en catálogo. El carrito de `ctrLlenarCarrito` no tiene rama para `40`. |

Total cobrado:

```
total = op_gravadas + op_exoneradas + op_inafectas + igv + icbper
```

Las operaciones gratuitas no se suman al total. El descuento global solo se aplica si hay base gravada: resta de `op_gravadas` y el IGV se recalcula sobre la base ya descontada. Puede ser un monto (`S/`) o un porcentaje; uno pone el otro en cero. Si el comprobante es solo exonerado o inafecto, el descuento global se bloquea. El factor de descuento se guarda con 5 decimales (`round(..., 5)`). Los montos de línea y de cabecera se redondean a 2 decimales con `round` de PHP al persistir y al armar el XML. El carrito en pantalla usa `number_format` después de calcular, así que la vista y el guardado no comparten el mismo redondeo intermedio.

Al guardar, el backend vuelve a calcular desde el carrito de sesión. No confía en el total pintado, pero sí confía en `precio_unitario`, `valor_unitario`, `igv`, `descuento_item` y `icbper` que llegaron del navegador y quedaron en sesión.

### ICBPER

Si el ítem marca `modoIcbper = s`, el impuesto a las bolsas es `round(0.30 * cantidad, 2)`. El 0.30 está fijo en `ajax/descuentos_items.ajax.php`; no sigue el monto anual de SUNAT. Se suma al total y no forma parte de la base del IGV.

### Precio de compra sugerido

Al tipear el precio de venta, `redondeos.ajax.php` propone `precio_compra = precio_unitario * 70 / 100`, redondeado a 2 decimales. Es una sugerencia de formulario, no una regla de la venta.

### Moneda y tipo de cambio

Monedas usadas: `PEN` y `USD`. Si la venta es en dólares, los precios del catálogo (en soles) se dividen entre el tipo de cambio antes de calcular. El tipo de cambio se consulta a `api.apis.net.pe` (`compra` y `venta`) y se guarda en `venta.tipocambio` como `float`. En soles se guarda `1`. El monto en letras usa `SOLES` o `DÓLARES` (`cantidad_en_letras.php`: convierte la parte entera y los céntimos a texto).

### Correlativos

Tabla `serie`: un correlativo por tipo y serie. Al emitir se usa `correlativo + 1` y se persiste ese valor antes de conocer el resultado de SUNAT. El número se consume aunque SUNAT rechace.

Series del volcado:

| tipocomp | serie | Uso |
| --- | --- | --- |
| `01` | `F002` | Factura |
| `03` | `B002` | Boleta |
| `07` | `FN01`, `BN02` | Nota de crédito (factura / boleta) |
| `08` | `FD02`, `BD01` | Nota de débito |
| `02` | `N001`, `N002` | Nota de venta (interna, sin XML) |
| `09` | `T002` | Guía de remisión |
| `00` | `C001` | Cotización |
| `RC` | `YYYYMMDD` | Resumen diario; el correlativo reinicia cuando cambia el día |
| `RA` | `YYYYMMDD` | Comunicación de baja; igual que el resumen |

Nombre de archivo: `{ruc}-{tipocomp}-{serie}-{correlativo}`.

### Validaciones al emitir

- Factura (`01`): el documento del cliente debe tener exactamente 11 caracteres. Si falta, pide RUC.
- Boleta (`03`): exactamente 8 caracteres. Si falta, pide DNI o «sin documento».
- Debe haber al menos una operación gravada, exonerada, inafecta o gratuita.
- Leyenda Selva (`bienesSelva` o `serviciosSelva` = `si`) solo si `op_gravadas` es 0. Si hay gravado, muestra el error SUNAT 3284 y no emite.
- Notas de venta (`02`) y cotizaciones (`00`) se guardan con la misma matemática, pero el XML y el envío solo corren para `01` y `03`.

No está implementado el tope de S/ 700 de la boleta, ni la validación de dígito verificador de RUC, ni el bloqueo de stock negativo. El stock es `int` y se resta la cantidad (decimal) al vender.

### Crédito y cuotas

`tipopago = Credito` muestra N cuotas. Cada cuota guarda fecha y monto en `pago_credito` (`fecha` y `cuota` son `text`). No hay validación de que la suma de cuotas sea igual al total, ni calendario de vencimientos en el XML más allá de la fecha de emisión.

### Stock y rechazo

Al insertar la venta se descuenta stock y se incrementa el contador de más vendidos. Si SUNAT responde con código de error entre 2000 y 3999, se devuelve el stock, se anulan los importes del detalle (se ponen en 0) y se marca la venta rechazada. El correlativo no se revierte. Si el código es menor a 2000 o hay falla de red, la venta queda para reenvío desde «Estados SUNAT» y el stock no se devuelve.

### Estados `feestado`

| Valor | Significado en el código |
| --- | --- |
| vacío | No enviado (solo firmado, nota interna, o aún no hay respuesta) |
| `1` | CDR con `ResponseCode` 0 (aceptado) |
| `2` | SOAP fault con código 2000–3999 (rechazado) |
| `3` | Falla de conexión u otro fault; se puede reenviar |
| otro | El `ResponseCode` del CDR cuando no es 0 |

Una venta cuenta como no enviada si `tipocomp` es `01` o `03`, `feestado` es vacío o `3`, `anulado = n` y `resumen = n`.

## 4. Flujo SUNAT

```
Carrito (sesión)
  → recalcular totales (ControladorVentas::ctrGuardarVenta)
  → GeneradorXML (UBL 2.1, CustomizationID 2.0)
  → Signature (XMLDSig enveloped, C14N, RSA-SHA1, certificado .pfx)
  → ZIP del XML
  → SOAP billService (sendBill / sendSummary)
  → leer CDR (ResponseCode, Description) o fault
  → guardar nombrexml, xmlbase64, cdrbase64 y feestado
  → PDF A4 o ticket (html2pdf) y correo (PHPMailer)
```

El modo sale de `emisor.modo`: `n` usa `https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService` y el usuario de prueba; otro valor usa `https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService` y el usuario SOL. La firma vive en `api/Signature.php`: lee el `.pfx` con `openssl_pkcs12_read`, firma el nodo `ExtensionContent` y deja el `Id` `SignatureSP`.

Documentos XML (`GeneradorXML`):

- Factura y boleta: `Invoice`, `UBLVersionID` 2.1, `InvoiceTypeCode` `01` o `03`, `listID` `0101`. Leyendas Selva `2001` y `2002`. Nota `1000` con el monto en letras.
- Nota de crédito y débito: documentos propios, con tipo, serie y correlativo de referencia y código de motivo.
- Resumen diario: `RC` + fecha `YYYYMMDD` + correlativo. Agrupa boletas del día (`tipocomp = 03`) que aún no tienen estado. El detalle usa condición de creación.
- Comunicación de baja: `RA` + fecha + correlativo, sobre una factura ya emitida (`ControladorEnvioSunat::ctrBajaComprobante`).
- Guía: hay dos generadores; el vigente es `CrearXMLGuiaRemision`. El envío no es el SOAP de facturas: pide token OAuth en `api-seguridad.sunat.gob.pe` con `client_id` / `secret_id` del emisor y consulta el ticket en la API REST de CPE.

La consulta de estado de un comprobante está en `ApiFacturacion::consultarComprobante`.

Catálogos que el XML necesita y ya están en la base:

- Tipo de comprobante: `01` factura, `03` boleta, `07` crédito, `08` débito, `09` guía, más `02` nota de venta y `00` cotización (internos).
- Tipo de documento de identidad: `0` sin documento, `1` DNI, `4` carné de extranjería, `6` RUC, `7` pasaporte, y `A`–`E`.
- Afectación IGV: códigos 10–17, 20–21, 30–36 y 40, con el tributo SUNAT (`1000` IGV, `9997` exonerado, `9998` inafecto, `9996` gratuito, `9995` exportación).
- Motivos de nota: `tabla_parametrica`, tipo `C` (crédito, 01–12) y `D` (débito, 01–03, 10–11).
- Medios de pago `001`–`013`, `101`–`108`, `999`.
- Motivo de traslado y modalidad (público `01`, privado `02`).
- Unidades UNECE en `unidad` (`NIU`, `ZZ`, `KGM`, etc.), con flag `activo`.
- Ubigeo de departamento, provincia y distrito.

## 5. Modelo de datos actual

Base `bdsistema`. Casi no hay claves foráneas: solo índices. Convivien `utf8_spanish_ci`, `latin1` y `utf8mb4`. Montos de negocio en `decimal(11,2)`; excepciones que no deben repetirse: `tipocambio float`, `guia.pesoTotal float`, `pago_credito.fecha/cuota` como `text`.

| Tabla | Rol |
| --- | --- |
| `emisor` | Un emisor: RUC, dirección, ubigeo, SOL, certificado, SMTP, IGV, modo, OAuth de guías, claves reCAPTCHA. |
| `usuarios` | Login, perfil texto, estado, `id_empresa`. Contraseña con `crypt()` y salt fijo `$2a$07$...`. |
| `serie` | Correlativo por tipo de comprobante. |
| `clientes` | Persona o empresa en la misma fila (`documento` o `ruc`). Contador `compras`. |
| `proveedores` | Igual que clientes, más chico. |
| `categorias`, `productos`, `unidad` | Catálogo. Producto guarda precio compra, valor, precio con IGV, IGV, stock, afectación y unidad. |
| `venta`, `detalle` | Cabecera y líneas de factura, boleta y nota de venta. |
| `pago_credito` | Cuotas de una venta. |
| `nota_credito`, `nota_credito_detalle`, `nota_debito`, `nota_debito_detalle` | Notas, con referencia al comprobante. |
| `compra`, `compra_detalle` | Compras. La serie la tipea el usuario (es el comprobante del proveedor). |
| `cotizaciones`, `detalle_cotizaciones` | Cotizaciones. `detalle_cotizaciones.idventa` apunta a la cotización, no a `venta`. |
| `guia`, `guia_detalle` | Guía y cantidades. |
| `envio_resumen`, `envio_resumen_detalle` | RC y RA. `condicion`: 1 creación, 2 actualización, 3 baja. |
| `tipo_comprobante`, `tipo_documento`, `tipo_afectacion`, `tabla_parametrica`, `medio_pago`, `motivo_traslado`, `modalidad_transporte` | Catálogos SUNAT. |
| `ubigeo_departamento`, `ubigeo_provincia`, `ubigeo_distrito` | Ubigeo. |

`venta` mezcla datos comerciales y el acuse de SUNAT (`feestado`, `fecodigoerror`, `femensajesunat`, `nombrexml`, `xmlbase64`, `cdrbase64`). `anulado` es `n`/`s`. `id_nc` e `id_nd` enlazan la venta con sus notas.

El volcado trae datos de personas, RUC, usuario SOL, clave de correo y tokens. Esos valores no se documentan aquí y no deben pasar al código nuevo.

## 6. Integraciones y secretos

| Integración | Dónde | Uso |
| --- | --- | --- |
| SUNAT billService beta / producción | `ApiFacturacion` | Factura, boleta, resumen, baja |
| SUNAT API seguridad + CPE | `ApiFacturacion::ObtenerToken`, `EnviarGuiaRemision` | Guías |
| `api.apifacturacion.com/ruc` y `/dni` | `ModeloClientes`, `ControladorEmpresa`, `ControladorUsuarios` | Consulta RUC y DNI. El token está escrito en el PHP. |
| `api.apis.net.pe` tipo de cambio | `Controladores/tipo-cambio.php` | Compra y venta del día. Token en el PHP. |
| Google reCAPTCHA | `ctrIngresoUsuario` | La verificación está desactivada (`if (True)`). |
| SMTP del emisor | `emisor.servidor`, puerto, clave | Envío de PDF con PHPMailer |
| PDF | `vistas/print/senda4.php` y tickets, vía `pdf/html2pdf` (TCPDF por debajo) | A4 y ticket |

El login compara la clave con `crypt($pass, '$2a$07$usesomesillystringforsalt$')`, pero esa comparación y el reCAPTCHA están comentados y reemplazados por `if (True)`. Cualquier usuario que exista entra sin validar la contraseña. El menú no filtra por `perfil`.

## 7. Huecos respecto de UBL 2.1

- Un solo emisor y series planas; no hay sucursales ni establecimientos (`AddressTypeCode` va fijo en `0000`).
- Cliente persona y empresa comparten columnas, y el tipo de documento del cliente en la venta es un `char(1)`.
- `detalle` no guarda código de producto, unidad ni descripción: se releen del producto. Si el producto cambia, el comprobante histórico cambia.
- Cuotas sin suma controlada y sin forma de pago UBL completa (`PaymentTerms` / `PaymentMeans`).
- ICBPER fijo en 0.30 y sin vigencia.
- `tipocambio` en `float`.
- Exportación (`40`) e IVAP (`17`) están en catálogo y no en el cálculo.
- No hay detracción, percepción ni retención.
- La firma es RSA-SHA1. SUNAT acepta ese perfil en muchos CPE, pero el esquema nuevo debería dejar el algoritmo explícito y configurable.
- XML y CDR se guardan como nombre de archivo y también como texto en columnas `text`, no como artefacto versionado.

## 8. Qué no migrar

Copias y librerías de terceros: `ControladorVentasO.php`, `ControladorVentasORI.php`, `ventas.controlador2.php`, `ventas.controlador2222222.php`, `ControladorProductosBACKUP.php`, `ControladorEnvioSunatBK.php`, `ControladorUsuarios2.php`, `ModeloClientes2.php`, `cantidad_en_letrasO.php`, `productos.ajaxBACKUP.php`, `ventasBACK.js`, `productosBACKUP.js`, `nota-debito00.js`, `panelO.php`, `* - copia.php`, `info.php`, `pdf/` (salvo como referencia visual de los HTML de impresión), `vistas/print/vendor`, `vistas/print/mike/vendor`, `vistas/pack/bower_components`.

`CrearXMLGuiaRemisionAntiguo` no es el generador vigente.

## 9. Decisiones que el código nuevo debe tomar

1. Recalcular en el backend con `BigDecimal` y redondeo half-up a 2 decimales por línea y por total. El frontend solo previsualiza.
2. Separar cliente persona y empresa, y separar el comprobante de su acuse SUNAT.
3. Correlativo en transacción, con bloqueo, y política explícita cuando SUNAT rechaza (hoy el número se pierde).
4. ICBPER con monto y vigencia, no con `0.30` fijo.
5. Credenciales, certificado y tokens solo por entorno. El volcado y los PHP actuales no sirven como configuración.
6. Confirmar, antes de la Fase 3, si se usa OpenUBL o JAXB propio. El starter publicado (`spring-boot-xsender`) documenta Jakarta XML SOAP 1.4.2 y WS 2.3.3, y el quickstart compila con Java 11. Eso no coincide con Spring Boot 4 y Jakarta EE 11.
