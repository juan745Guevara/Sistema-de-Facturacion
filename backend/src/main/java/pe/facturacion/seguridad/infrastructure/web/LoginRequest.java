package pe.facturacion.seguridad.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record LoginRequest(
		@NotBlank @Size(max = 50) String username,
		@NotBlank @Size(max = 100) String password) {
}
