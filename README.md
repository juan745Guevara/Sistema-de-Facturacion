# Sistema de Facturación Electrónica

Migración de OPENFACT (facturación electrónica SUNAT, Perú) desde PHP 8 + MySQL hacia un monolito modular en Spring Boot y un frontend en Angular.

El sistema PHP original está en `codigo-antiguo/` y se usa solo como referencia. No se modifica.

## Estructura

```
Sistema-de-Facturacion/
├── codigo-antiguo/   sistema PHP actual (solo lectura)
├── backend/          Spring Boot (desde la Fase 1)
├── frontend/         Angular (desde la Fase 1)
├── docs/             análisis y decisiones
├── README.md
└── .gitignore
```

## Estado

| Fase | Contenido | Estado |
| --- | --- | --- |
| 0 | Reorganización y análisis del legado | Hecha |
| 1 | Esqueleto backend y Angular | Hecha |
| 2 | Empresa, catálogo, clientes, proveedores (y usuarios) | Hecha |
| 3 | Ventas y SUNAT (factura y boleta) | Hecha |
| 4 | Notas, resumen diario y comunicación de baja | Hecha |
| 5 | Guías, compras y cotizaciones | Hecha |
| 6 | Reportes, dashboard, PDF, Excel y correo | Hecha |
| 7 | Migración de datos, README final y revisión | Hecha |

El análisis de reglas de negocio, tablas y flujo SUNAT está en [docs/analisis-sistema-antiguo.md](docs/analisis-sistema-antiguo.md).

## Cómo correr el backend

Requiere JDK 25 o superior y Docker. El proyecto compila con `release 25`; con JDK 27 también funciona gracias a Lombok 1.18.48.

```powershell
cd backend
Copy-Item .env.example .env        # completar DB_PASSWORD, JWT_SECRET y ADMIN_*
docker compose -f docker-compose.dev.yml --env-file .env up -d
.\mvnw.cmd spring-boot:run
```

- El perfil `dev` está activo por defecto, lee `backend/.env` y apunta a SUNAT beta.
- Flyway crea el esquema al arrancar.
- Si la tabla de usuarios está vacía, se crea el administrador definido en `ADMIN_USERNAME` y `ADMIN_PASSWORD` (mínimo 12 caracteres).
- La API queda en `http://localhost:8080/api` y la documentación en `http://localhost:8080/swagger-ui.html`.

- La consulta de RUC/DNI (botón de búsqueda en empresa, clientes y proveedores) usa `CONSULTA_DOCUMENTOS_URL` y `CONSULTA_DOCUMENTOS_TOKEN`. Sin token, la API responde 503 y el resto del sistema funciona igual.
- El certificado, las credenciales SOL y el SMTP solo se configuran con variables de entorno; no se guardan en la base de datos.
- Sin `SMTP_HOST`, el resto del sistema funciona y `POST /api/reportes/correo` responde 503.

Tests: `.\mvnw.cmd test`. Los de integración usan Testcontainers y se omiten si Docker no está corriendo.

### Permisos por rol

| Recurso | Consultar | Crear y editar | Eliminar |
| --- | --- | --- | --- |
| Empresa | Todos | ADMINISTRADOR | — |
| Categorías y productos | Todos | ADMINISTRADOR, ESPECIAL | ADMINISTRADOR, ESPECIAL |
| Unidades de medida | Todos | Activar o desactivar: ADMINISTRADOR | — |
| Clientes | Todos | Todos | ADMINISTRADOR, ESPECIAL |
| Proveedores | Todos | ADMINISTRADOR, ESPECIAL | ADMINISTRADOR, ESPECIAL |
| Usuarios | ADMINISTRADOR | ADMINISTRADOR | No se eliminan, se desactivan |
| Ventas, notas, compras, cotizaciones, guías, reportes | Todos los autenticados | Todos los autenticados | Anular compra: autenticado |

Un usuario desactivado conserva su token hasta que vence (8 horas).

## Cómo correr el frontend

Requiere Node 22 o superior.

```powershell
cd frontend
npm install
npm start          # http://localhost:4200, llama al backend en localhost:8080
```

Tests: `npm test`. Build de producción: `npm run build`. En producción, el frontend espera el API en `/api`, en el mismo dominio.

## Cómo correr el sistema antiguo

Requiere PHP 8, MariaDB/MySQL y Apache (Laragon, XAMPP o WAMP).

1. Apuntar el document root a `codigo-antiguo/`.
2. Restaurar `codigo-antiguo/BD/bdsistema.sql`.
3. Ajustar la conexión en `codigo-antiguo/Conect/Conexion.php`.
4. Abrir la aplicación en el navegador.

El volcado y varios PHP contienen credenciales y tokens. No los copies al código nuevo: van en variables de entorno.

## Stack

Backend: Java 25, Spring Boot 4.1, Maven, PostgreSQL 16, Spring Data JPA, Flyway, Spring Security con JWT (resource server, HS256), MapStruct, Lombok, Springdoc y Spring Modulith. Cada módulo vive en `pe.facturacion.<modulo>` con capas `domain`, `application` e `infrastructure`, y tiene su propio esquema en PostgreSQL. `ModularidadTest` verifica que ningún módulo use clases internas de otro.

Frontend: Angular 22 (standalone, signals, zoneless), PrimeNG 22 con el tema Aura, PrimeFlex, formularios reactivos, interceptor JWT, guards por rol y carga diferida por feature. Los tests usan Vitest.

OpenUBL (XBuilder/XSender) no encaja con Spring Boot 4 ni Jakarta EE 11. El XML UBL 2.1, la firma XMLDSig y el SOAP de `billService` se implementaron a mano con DOM, `javax.xml.crypto` y `RestClient`.

### Notas y lotes SUNAT (Fase 4)

- Nota de crédito (`07`) y de débito (`08`) sobre factura o boleta, con motivo de los catálogos 09 y 10.
- La serie de la nota debe empezar con F o B según el comprobante de origen. Una anulación o devolución total copia las líneas y, si aplica, devuelve stock.
- Resumen diario `RC` de boletas pendientes (`sendSummary` + `getStatus`) y comunicación de baja `RA` de facturas y notas aceptadas.

### Guías, compras y cotizaciones (Fase 5)

- Compra: el usuario tipea serie y número del proveedor. Aumenta stock al registrar y lo revierte al anular. No se envía a SUNAT.
- Cotización: serie interna `CT` (`C001`). No mueve stock ni se envía a SUNAT.
- Guía de remisión remitente: serie `T***`, ubigeo de partida y llegada. Queda `PENDIENTE` (el envío REST OAuth a SUNAT no está cableado).
- Ubigeo: `GET /api/ubigeos?q=` sobre un catálogo sembrado (Lima y otras ciudades).

### Reportes (Fase 6)

- Dashboard en inicio: ventas del día, comprobantes `PENDIENTE`, compras del mes.
- `GET /api/reportes/ventas` y `/compras` con Excel (`.xlsx`) y PDF (`.pdf`).
- `POST /api/reportes/correo` adjunta el PDF si hay SMTP.

### Migración de datos (Fase 7)

El mapeo MySQL → PostgreSQL está en [backend/scripts/migracion-datos/README.md](backend/scripts/migracion-datos/README.md). Las contraseñas del legado no se reutilizan: hay que generar BCrypt nuevos.

### Ventas (Fase 3)

- Factura (`01`), boleta (`03`) y nota de venta (`NV`). El backend recalcula todos los importes; el frontend solo previsualiza.
- Factura exige RUC. Boleta sin documento no puede llegar a S/ 700. El correlativo se consume aunque SUNAT rechace.
- Sin certificado o usuario SOL la venta queda `PENDIENTE` y se reenvía desde Estados SUNAT.
- El stock se descuenta al emitir y solo se devuelve si el CDR queda `RECHAZADO` o `ANULADO`.
