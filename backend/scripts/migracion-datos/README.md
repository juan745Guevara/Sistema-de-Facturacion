# Migración de datos (PHP / MySQL → PostgreSQL)

El sistema antiguo (`codigo-antiguo/BD/bdsistema.sql`) no se importa automáticamente. Este directorio documenta el mapeo para un volcado controlado.

No copies certificados, tokens ni claves SOL al código. Van en variables de entorno.

## Correspondencia de tablas

| Origen (MySQL) | Destino (PostgreSQL) | Notas |
| --- | --- | --- |
| `emisor` | `empresa.empresa` | Un solo emisor. Ubigeo en columnas de domicilio. |
| `usuarios` | `seguridad.usuario` | Recalcular hashes con BCrypt. No reutilizar MD5/texto plano. |
| `categorias`, `productos`, `unidad` | `catalogo.categoria`, `catalogo.producto`, `catalogo.unidad_medida` | Unidades SUNAT ya están sembradas; solo activar las usadas. |
| `clientes` | `clientes.cliente` | Unificar `documento`/`ruc` en `tipo_documento` + `numero`. |
| `proveedores` | `proveedores.proveedor` | |
| `venta`, `detalle` | `ventas.venta`, `ventas.linea` | Recalcular importes en el backend si hay dudas. `feestado` → `estado_sunat`. |
| `pago_credito` | `ventas.cuota` | |
| `nota_credito` / `nota_debito` y sus detalles | `notas.nota`, `notas.linea` | Motivo catálogo 09/10. |
| `resumen` / líneas de baja | `sunat.lote`, `sunat.lote_linea` | Correlativo diario `YYYYMMDD`. |
| `compra`, `compra_detalle` | `compras.compra`, `compras.linea` | Serie y número los escribe el proveedor. |
| `cotizaciones`, `detalle_cotizaciones` | `cotizaciones.cotizacion`, `cotizaciones.linea` | Tipo interno `CT`. |
| `guia`, `guia_detalle` | `guias.guia`, `guias.linea` | Serie `T***`. El envío REST OAuth a SUNAT no se migra: quedan `PENDIENTE`. |
| ubigeo (si existía) | `shared.ubigeo` | Ya hay un semilla de distritos frecuentes. |

## Orden sugerido

1. Empresa y series (`empresa.serie`).
2. Usuarios (nuevas contraseñas).
3. Catálogo.
4. Clientes y proveedores.
5. Ventas y cuotas.
6. Notas y lotes SUNAT.
7. Compras, cotizaciones y guías.

Los XML y CDR en Base64 del legado se pueden guardar fuera de la base (objeto o archivo) y referenciar después. No los versions en git.

## Ejemplo de extracción

```sql
-- En MySQL, solo lectura
SELECT ruc, razon_social, direccion FROM emisor;
SELECT usuario, nombre, perfil FROM usuarios;
```

La carga al nuevo esquema se hace con `COPY` o un script ETL propio. Flyway ya crea las tablas vacías.
