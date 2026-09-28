package pe.facturacion.sunat.application.port.out;

import pe.facturacion.sunat.domain.model.Emisor;
import pe.facturacion.sunat.domain.model.LoteSunat;

public interface GeneradorXmlLotePort {

	byte[] generar(LoteSunat lote, Emisor emisor);

}
