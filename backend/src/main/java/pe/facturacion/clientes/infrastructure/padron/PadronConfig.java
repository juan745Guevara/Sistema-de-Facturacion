package pe.facturacion.clientes.infrastructure.padron;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import pe.facturacion.clientes.application.port.out.PadronDocumentosPort;

@Configuration(proxyBeanMethods = false)
class PadronConfig {

	@Bean
	PadronDocumentosPort padronDocumentosPort(RestClient.Builder builder, ConsultaDocumentosProperties propiedades) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(propiedades.timeout()).build());
		fabrica.setReadTimeout(propiedades.timeout());
		RestClient http = builder.clone().baseUrl(propiedades.url().toString()).requestFactory(fabrica).build();
		return new ApiFacturacionPadronAdapter(http, propiedades.token());
	}

}
