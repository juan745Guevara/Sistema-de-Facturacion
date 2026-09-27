package pe.facturacion.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DineroTest {

	@Test
	void redondeaADosDecimalesHalfUp() {
		assertThat(Dinero.de("10.005", Moneda.PEN).monto()).isEqualByComparingTo("10.01");
		assertThat(Dinero.de("10.004", Moneda.PEN).monto()).isEqualByComparingTo("10.00");
		assertThat(Dinero.de("5", Moneda.PEN).monto().scale()).isEqualTo(2);
	}

	@Test
	void sumaYRestaEnLaMismaMoneda() {
		Dinero base = Dinero.de("53.39", Moneda.PEN);
		Dinero igv = Dinero.de("9.61", Moneda.PEN);

		assertThat(base.sumar(igv)).isEqualTo(Dinero.de("63.00", Moneda.PEN));
		assertThat(base.restar(igv)).isEqualTo(Dinero.de("43.78", Moneda.PEN));
	}

	@Test
	void multiplicarRedondeaElResultado() {
		Dinero base = Dinero.de("53.39", Moneda.PEN);

		assertThat(base.multiplicar(new BigDecimal("0.18")).monto()).isEqualByComparingTo("9.61");
	}

	@Test
	void noPermiteMezclarMonedas() {
		Dinero soles = Dinero.de("1", Moneda.PEN);
		Dinero dolares = Dinero.de("1", Moneda.USD);

		assertThatThrownBy(() -> soles.sumar(dolares)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> soles.esMayorQue(dolares)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void igualdadIgnoraLaEscalaDeEntrada() {
		assertThat(Dinero.de("63", Moneda.PEN)).isEqualTo(Dinero.de("63.000", Moneda.PEN));
	}

	@Test
	void consultas() {
		assertThat(Dinero.cero(Moneda.USD).esCero()).isTrue();
		assertThat(Dinero.de("-0.01", Moneda.PEN).esNegativo()).isTrue();
		assertThat(Dinero.de("2", Moneda.PEN).esMayorQue(Dinero.de("1.99", Moneda.PEN))).isTrue();
		assertThat(Dinero.de("63", Moneda.PEN)).hasToString("S/ 63.00");
	}

}
