package pe.facturacion.sunat.application.usecase;

import pe.facturacion.shared.domain.exception.DominioException;
import pe.facturacion.sunat.application.dto.ComprobanteElectronico;
import pe.facturacion.sunat.application.port.in.RegistrarDocumentoUseCase;
import pe.facturacion.sunat.application.port.out.DocumentoElectronicoRepositoryPort;
import pe.facturacion.sunat.domain.model.DocumentoElectronico;

public class RegistrarDocumentoService implements RegistrarDocumentoUseCase {

	private final DocumentoElectronicoRepositoryPort documentos;

	public RegistrarDocumentoService(DocumentoElectronicoRepositoryPort documentos) {
		this.documentos = documentos;
	}

	@Override
	public void registrar(ComprobanteElectronico comprobante) {
		if (documentos.existe(comprobante.tipo(), comprobante.serie(), comprobante.correlativo())) {
			throw DominioException.conflicto("documento-duplicado",
					"Ya existe el %s %s".formatted(comprobante.tipo().descripcion(), comprobante.numero()));
		}
		documentos.registrar(DocumentoElectronico.pendiente(comprobante.tipo(), comprobante.serie(),
				comprobante.correlativo(), comprobante.fechaEmision()), comprobante);
	}

}
