package pe.facturacion.sunat.application.usecase;

import pe.facturacion.shared.domain.exception.RecursoNoEncontradoException;
import pe.facturacion.shared.domain.model.sunat.TipoComprobante;
import pe.facturacion.sunat.application.dto.DocumentoElectronicoDto;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;

final class Documentos {

	private Documentos() {
	}

	static DocumentoElectronico obtener(DocumentoElectronicoRepositoryPort documentos, TipoComprobante tipo,
			String serie, int correlativo) {
		return documentos.buscar(tipo, serie, correlativo)
				.orElseThrow(() -> new RecursoNoEncontradoException("Documento electrónico",
						"%s %s-%d".formatted(tipo.descripcion(), serie, correlativo)));
	}

	static DocumentoElectronicoDto aDto(DocumentoElectronico documento) {
		return new DocumentoElectronicoDto(documento.id(), documento.tipo(), documento.serie(),
				documento.correlativo(), documento.fechaEmision(), documento.estado(), documento.codigoRespuesta(),
				documento.mensaje(), documento.observaciones(), documento.hash(), documento.intentos(),
				documento.ultimoEnvio(), documento.firmado(), documento.cdr() != null);
	}

}
