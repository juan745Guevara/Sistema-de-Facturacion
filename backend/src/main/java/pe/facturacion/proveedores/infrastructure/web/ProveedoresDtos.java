package pe.facturacion.proveedores.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

final class ProveedoresDtos {

	private ProveedoresDtos() {
	}

	record ProveedorRequest(
			@NotNull TipoDocumentoIdentidad tipoDocumento,
			@Size(max = 15) String numeroDocumento,
			@NotBlank @Size(max = 200) String nombre,
			@Size(max = 200) String direccion,
			@Email @Size(max = 120) String email,
			@Size(max = 20) String telefono) {
	}

	record ProveedorResponse(
			Long id,
			TipoDocumentoIdentidad tipoDocumento,
			String numeroDocumento,
			String nombre,
			String direccion,
			String email,
			String telefono) {
	}

}
