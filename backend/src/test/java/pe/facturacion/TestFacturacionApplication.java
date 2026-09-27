package pe.facturacion;

import org.springframework.boot.SpringApplication;

/** Arranca la aplicación contra un PostgreSQL de Testcontainers: {@code ./mvnw spring-boot:test-run}. */
public class TestFacturacionApplication {

	public static void main(String[] args) {
		SpringApplication.from(FacturacionApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}

}
