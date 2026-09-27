package pe.facturacion.clientes.infrastructure.web;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import pe.facturacion.shared.domain.model.sunat.TipoDocumentoIdentidad;

final class ClientesDtos {

	private ClientesDtos() {
	}

	record ClienteRequest(
			@NotNull TipoDocumentoIdentidad tipoDocumento,
			@Size(max = 15) String numeroDocumento,
			@NotBlank @Size(max = 200) String nombre,
			@Size(max = 200) String direccion,
			@Email @Size(max = 120) String email,
			@Size(max = 20) String telefono,
			@Past LocalDate fechaNacimiento) {
	}

	record ClienteResponse(
			Long id,
			TipoDocumentoIdentidad tipoDocumento,
			String numeroDocumento,
			String nombre,
			String direccion,
			String email,
			String telefono,
			LocalDate fechaNacimiento) {
	}

	record DatosPadronResponse(
			TipoDocumentoIdentidad tipoDocumento,
			String numeroDocumento,
			String nombre,
			String direccion,
			String ubigeo,
			String departamento,
			String provincia,
			String distrito,
			String estado,
			String condicion) {
	}

}
