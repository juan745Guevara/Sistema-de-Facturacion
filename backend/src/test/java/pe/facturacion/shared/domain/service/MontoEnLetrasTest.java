package pe.facturacion.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import pe.facturacion.shared.domain.model.Dinero;
import pe.facturacion.shared.domain.model.Moneda;

class MontoEnLetrasTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource(delimiter = '|', textBlock = """
			0          | CERO CON 00/100 SOLES
			0.5        | CERO CON 50/100 SOLES
			1          | UN CON 00/100 SOLES
			15         | QUINCE CON 00/100 SOLES
			21         | VEINTIUNO CON 00/100 SOLES
			31         | TREINTA Y UNO CON 00/100 SOLES
			53.4       | CINCUENTA Y TRES CON 40/100 SOLES
			63.00      | SESENTA Y TRES CON 00/100 SOLES
			100        | CIEN CON 00/100 SOLES
			101        | CIENTO UNO CON 00/100 SOLES
			236        | DOSCIENTOS TREINTA Y SEIS CON 00/100 SOLES
			1000       | MIL CON 00/100 SOLES
			1001       | MIL UNO CON 00/100 SOLES
			21000      | VEINTIUN MIL CON 00/100 SOLES
			31500      | TREINTA Y UN MIL QUINIENTOS CON 00/100 SOLES
			101000     | CIENTO UN MIL CON 00/100 SOLES
			365800     | TRESCIENTOS SESENTA Y CINCO MIL OCHOCIENTOS CON 00/100 SOLES
			1000000    | UN MILLON CON 00/100 SOLES
			21000000   | VEINTIUN MILLONES CON 00/100 SOLES
			10.005     | DIEZ CON 01/100 SOLES
			""")
	void convierteMontosEnSoles(String monto, String esperado) {
		assertThat(MontoEnLetras.convertir(new BigDecimal(monto), Moneda.PEN)).isEqualTo(esperado);
	}

	@Test
	void usaElNombreDeLaMoneda() {
		assertThat(MontoEnLetras.convertir(Dinero.de("2501101.55", Moneda.USD)))
				.isEqualTo("DOS MILLONES QUINIENTOS UN MIL CIENTO UNO CON 55/100 DÓLARES");
	}

	@Test
	void rechazaNegativos() {
		assertThatThrownBy(() -> MontoEnLetras.convertir(new BigDecimal("-1"), Moneda.PEN))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
