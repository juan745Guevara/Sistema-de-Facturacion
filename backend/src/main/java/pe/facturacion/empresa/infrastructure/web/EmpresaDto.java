package pe.facturacion.empresa.infrastructure.web;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record EmpresaDto(
		@NotBlank @Pattern(regexp = "\\d{11}", message = "debe tener 11 dígitos") String ruc,
		@NotBlank @Size(max = 200) String razonSocial,
		@Size(max = 200) String nombreComercial,
		@NotNull @Valid DireccionDto domicilioFiscal,
		@Size(max = 20) String telefono,
		@Email @Size(max = 120) String correoVentas,
		@Email @Size(max = 120) String correoSoporte,
		@NotNull @DecimalMin("0") @DecimalMax(value = "100", inclusive = false) @Digits(integer = 2, fraction = 2)
		BigDecimal porcentajeIgv,
		boolean bienesSelva,
		boolean serviciosSelva) {

	record DireccionDto(
			@NotBlank @Size(max = 200) String direccion,
			@NotBlank @Pattern(regexp = "\\d{6}", message = "debe tener 6 dígitos") String ubigeo,
			@NotBlank @Size(max = 60) String departamento,
			@NotBlank @Size(max = 60) String provincia,
			@NotBlank @Size(max = 60) String distrito,
			@Pattern(regexp = "[A-Za-z]{2}", message = "debe tener 2 letras") String codigoPais,
			@Pattern(regexp = "\\d{4}", message = "debe tener 4 dígitos") String codigoEstablecimiento) {
	}

}
