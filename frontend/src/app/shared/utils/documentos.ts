import { TipoDocumentoIdentidad } from '../../core/api/catalogos-sunat.service';
import { esDniValido, esRucValido } from '../validators/documento.validators';

const ABREVIATURAS: Partial<Record<TipoDocumentoIdentidad, string>> = {
  SIN_DOCUMENTO: 'S/D',
  CARNET_EXTRANJERIA: 'CE',
  PASAPORTE: 'PAS',
  CEDULA_DIPLOMATICA: 'CD',
  DOC_PAIS_RESIDENCIA: 'DOC',
};

export function abreviaturaDocumento(tipo: TipoDocumentoIdentidad): string {
  return ABREVIATURAS[tipo] ?? tipo;
}

/** Solo DNI y RUC se pueden consultar en RENIEC/SUNAT. */
export function documentoConsultable(tipo: string | null, numero: string): 'DNI' | 'RUC' | null {
  const limpio = numero.trim();
  if (tipo === 'RUC' && esRucValido(limpio)) {
    return 'RUC';
  }
  if (tipo === 'DNI' && esDniValido(limpio)) {
    return 'DNI';
  }
  return null;
}
