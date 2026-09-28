package pe.facturacion.shared.infrastructure.web;

import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pe.facturacion.shared.domain.model.Ubigeo;
import pe.facturacion.shared.infrastructure.persistence.UbigeoJpaRepository;

@Tag(name = "Ubigeo")
@RestController
@RequestMapping("/api/ubigeos")
@RequiredArgsConstructor
class UbigeoController {

	private final UbigeoJpaRepository ubigeos;

	@Operation(summary = "Busca distritos por código o nombre")
	@GetMapping
	List<Ubigeo> buscar(@RequestParam(required = false) String q) {
		String patron = q == null || q.isBlank() ? null : "%" + q.toLowerCase(Locale.ROOT) + "%";
		return ubigeos.buscar(patron).stream()
				.map(u -> new Ubigeo(u.getCodigo(), u.getDepartamento(), u.getProvincia(), u.getDistrito()))
				.limit(50)
				.toList();
	}

}
