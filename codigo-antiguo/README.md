# 📄 FACTURACIÓN ELECTRÓNICA - OPENFACT

Sistema de **Facturación Electrónica gratuito** orientado a fines **educativos y académicos**, desarrollado como parte del curso **Construcción de Software I**.

El proyecto aborda conceptos clave del desarrollo de software y la facturación electrónica en el contexto del **Perú**.

---

## 🎯 Objetivos del proyecto

Este software se utiliza como material de estudio para los siguientes temas:

- Diseño de **Front-End**
- Manejo de archivos **XML** y **CSV**
- Respuestas asíncronas desde el servidor usando **AJAX**
- Conexión a **bases de datos**
- Implementación de **UBL** y **Facturación Electrónica en Perú**

---

## ⚙️ Requisitos

Para ejecutar el sistema necesitas:

- **PHP 8.0 o superior**
- **MySQL 8.0 o superior**
- **Apache 2.4 o superior**

💡 Puedes usar paquetes preconfigurados como:
- WAMP
- XAMPP
- **Laragon** *(recomendado)*

---

## 🚀 Instalación

1. Ubícate en la carpeta `www` de tu servidor web y clona el repositorio:

   ```bash
   git clone https://github.com/hubelsolis/facturacion.git

2. Restaura la base de datos:

   En la carpeta BD se encuentra el archivo bdsistema.sql

   Puedes restaurarlo usando:
   - PhpMyAdmin
   - HeidiSQL
   - u otro gestor compatible con MySQL

   (*) la configuracion de conexion a la DB esta en la carpeta Conect\conexion.php, por si le pones clave o cambias los puertos

3. Inicia tu servidor web y accede desde el navegador a:
   http://localhost/facturacion

   Si estas en laragon puede acceder a las aplicaciones en desarrollo mediante su icono verde en la barra de tareas / www / facturacion 

5. Pruebas Unitarias

   🔧 En implementación.

6. Calidad de Software

   🔧 En implementación.

7. Mantenimiento y Soporte

   El mantenimiento y servicio postventa se realiza según acuerdo entre cliente y distribuidor.

8. Licencia

   Proyecto de uso libre y educativo.

## 🧠 Qué mejoré y por qué
✔️ Mejoras en arquitectura y llamadas asincronas
- Estructura clara de MVC, quisiera haberme subido a laravel pero es un lio el cambio de versiones en los hosting web.
- Actualmente estoy cambiando los ajax por fetch, hay algunos errores que estare subsanando, el motivo dejar de lado a Jquery
- A futuro deseo cambiar la estructura de la DB por una 100% compatible con UBL y la RS 236-2004 de SUNAT.

✔️ Títulos más claros
- Uso de emojis moderados (GitHub-friendly)
- Jerarquía correcta (`#`, `##`)

✔️ Markdown correcto
- Bloques de código para comandos
- URLs claras
- Listas limpias

✔️ Imagen más seria del proyecto
- Ideal para portafolio o referencia universitaria

