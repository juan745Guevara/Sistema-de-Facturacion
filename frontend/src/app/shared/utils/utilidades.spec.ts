import { abreviaturaDocumento, documentoConsultable } from './documentos';
import { aFechaIso, desdeFechaIso } from './fechas';
import { sinVacios } from './formularios';

describe('sinVacios', () => {
  it('recorta los textos y convierte los vacíos en null sin tocar otros tipos', () => {
    expect(sinVacios({ nombre: '  Ana ', email: '   ', stock: 0, activo: false, fecha: null })).toEqual({
      nombre: 'Ana',
      email: null,
      stock: 0,
      activo: false,
      fecha: null,
    });
  });
});

describe('fechas ISO', () => {
  it('convierte en ambos sentidos con la fecha local', () => {
    expect(aFechaIso(new Date(1990, 0, 5))).toBe('1990-01-05');
    expect(desdeFechaIso('1990-01-05')).toEqual(new Date(1990, 0, 5));
  });

  it('acepta valores vacíos', () => {
    expect(aFechaIso(null)).toBeNull();
    expect(desdeFechaIso(null)).toBeNull();
    expect(desdeFechaIso('05/01/1990')).toBeNull();
  });
});

describe('documentos', () => {
  it('solo permite consultar DNI y RUC válidos', () => {
    expect(documentoConsultable('RUC', ' 20601487871 ')).toBe('RUC');
    expect(documentoConsultable('DNI', '47204426')).toBe('DNI');
    expect(documentoConsultable('RUC', '20601487872')).toBeNull();
    expect(documentoConsultable('PASAPORTE', '47204426')).toBeNull();
  });

  it('abrevia los tipos largos', () => {
    expect(abreviaturaDocumento('CARNET_EXTRANJERIA')).toBe('CE');
    expect(abreviaturaDocumento('RUC')).toBe('RUC');
  });
});
