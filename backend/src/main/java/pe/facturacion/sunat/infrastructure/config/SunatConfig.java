package pe.facturacion.sunat.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import pe.facturacion.empresa.application.port.in.GestionarEmpresaUseCase;
import pe.facturacion.shared.application.port.out.Transacciones;
import pe.facturacion.sunat.application.port.in.ConsultarDocumentosUseCase;
import pe.facturacion.sunat.application.port.in.EnviarDocumentoUseCase;
import pe.facturacion.sunat.application.port.in.GestionarLotesUseCase;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.application.port.out.EmisorPort;
import pe.facturacion.sunat.application.port.out.EventosPort;
import pe.facturacion.sunat.application.port.out.FirmaDigitalPort;
import pe.facturacion.sunat.application.port.out.GeneradorXmlLotePort;
import pe.facturacion.sunat.application.port.out.GeneradorXmlPort;
import pe.facturacion.sunat.application.port.out.LoteRepositoryPort;
import pe.facturacion.sunat.application.port.out.ServicioSunatPort;
import pe.facturacion.sunat.application.usecase.ConsultarDocumentosService;
import pe.facturacion.sunat.application.usecase.EnviarDocumentoService;
import pe.facturacion.sunat.application.usecase.GestionarLotesService;
import pe.facturacion.sunat.application.usecase.RegistrarDocumentoService;
import pe.facturacion.sunat.domain.model.Emisor;
import pe.facturacion.sunat.infrastructure.firma.FirmaXmlDsig;
import pe.facturacion.sunat.infrastructure.soap.ClienteSoapSunat;

@Configuration(proxyBeanMethods = false)
class SunatConfig {

	@Bean
	RegistrarDocumentoUseCase registrarDocumentoUseCase(DocumentoElectronicoRepositoryPort documentos) {
		return new RegistrarDocumentoService(documentos);
	}

	@Bean
	ConsultarDocumentosUseCase consultarDocumentosUseCase(DocumentoElectronicoRepositoryPort documentos,
			EmisorPort emisores) {
		return new ConsultarDocumentosService(documentos, emisores);
	}

	@Bean
	EnviarDocumentoUseCase enviarDocumentoUseCase(DocumentoElectronicoRepositoryPort documentos, EmisorPort emisores,
			GeneradorXmlPort generador, FirmaDigitalPort firma, ServicioSunatPort sunat, EventosPort eventos,
			Transacciones transacciones, Clock reloj) {
		return new EnviarDocumentoService(documentos, emisores, generador, firma, sunat, eventos, transacciones, reloj);
	}

	@Bean
	GestionarLotesUseCase gestionarLotesUseCase(DocumentoElectronicoRepositoryPort documentos, LoteRepositoryPort lotes,
			EmisorPort emisores, GeneradorXmlLotePort generador, FirmaDigitalPort firma, ServicioSunatPort sunat,
			EventosPort eventos, Transacciones transacciones, Clock reloj) {
		return new GestionarLotesService(documentos, lotes, emisores, generador, firma, sunat, eventos, transacciones,
				reloj);
	}

	@Bean
	EmisorPort emisorPort(GestionarEmpresaUseCase empresas) {
		return () -> {
			var empresa = empresas.obtener();
			var domicilio = empresa.domicilioFiscal();
			return new Emisor(empresa.ruc(), empresa.razonSocial(),
					empresa.nombreComercial() == null ? empresa.razonSocial() : empresa.nombreComercial(),
					domicilio.direccion(), domicilio.ubigeo(), domicilio.departamento(), domicilio.provincia(),
					domicilio.distrito(), domicilio.codigoPais(), domicilio.codigoEstablecimiento());
		};
	}

	@Bean
	EventosPort eventosPort(ApplicationEventPublisher publicador) {
		return publicador::publishEvent;
	}

	@Bean
	FirmaDigitalPort firmaDigitalPort(SunatProperties propiedades) {
		var cert = propiedades.certificado();
		return new FirmaXmlDsig(cert == null ? null : cert.ruta(), cert == null ? null : cert.clave(),
				cert == null ? null : cert.algoritmo());
	}

	@Bean
	ServicioSunatPort servicioSunatPort(SunatProperties propiedades, RestClient.Builder builder) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(propiedades.timeout()).build());
		fabrica.setReadTimeout(propiedades.timeout());
		RestClient cliente = builder.clone().baseUrl(propiedades.urlFacturacion()).requestFactory(fabrica).build();
		return new ClienteSoapSunat(cliente, propiedades.usuarioSol(), propiedades.claveSol());
	}

}
