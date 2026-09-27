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
| 1 | Esqueleto backend y Angular | Pendiente |
| 2 | Empresa, catálogo, clientes, proveedores | Pendiente |
| 3 | Ventas y SUNAT (factura y boleta) | Pendiente |
| 4 | Notas, resumen diario y comunicación de baja | Pendiente |
| 5 | Guías, compras y cotizaciones | Pendiente |
| 6 | Reportes, dashboard, PDF, Excel y correo | Pendiente |
| 7 | Migración de datos, README final y revisión | Pendiente |

El análisis de reglas de negocio, tablas y flujo SUNAT está en [docs/analisis-sistema-antiguo.md](docs/analisis-sistema-antiguo.md).

## Cómo correr el sistema antiguo

Requiere PHP 8, MariaDB/MySQL y Apache (Laragon, XAMPP o WAMP).

1. Apuntar el document root a `codigo-antiguo/`.
2. Restaurar `codigo-antiguo/BD/bdsistema.sql`.
3. Ajustar la conexión en `codigo-antiguo/Conect/Conexion.php`.
4. Abrir la aplicación en el navegador.

El volcado y varios PHP contienen credenciales y tokens. No los copies al código nuevo: van en variables de entorno.

## Stack previsto

Backend: Java 25, Spring Boot 4.1, Maven, PostgreSQL 16, Spring Data JPA, Flyway, Spring Security + JWT, MapStruct, Lombok, Springdoc, Spring Modulith.

Frontend: Angular (standalone, signals), PrimeNG, formularios reactivos, interceptor JWT.

XBuilder y XSender de Project OpenUBL publican un starter pensado para Jakarta XML SOAP 1.4 / WS 2.3 y un quickstart en Java 11. Eso no está alineado con Spring Boot 4 y Jakarta EE 11. No se reemplazan por una implementación propia hasta confirmarlo antes de la Fase 3.
