package pe.facturacion.seguridad.infrastructure.web;

import pe.facturacion.seguridad.domain.model.Rol;

record UsuarioResponse(Long id, String nombre, String username, Rol rol) {
}
