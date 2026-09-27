import { desglosarPrecio, precioCompraSugerido } from './precios';

describe('desglosarPrecio', () => {
  it('separa el IGV incluido en un precio gravado', () => {
    expect(desglosarPrecio(118, 'GRAVADO_ONEROSO', 18)).toEqual({ valorUnitario: 100, impuesto: 18, tasa: 18 });
    expect(desglosarPrecio(3.5, 'GRAVADO_ONEROSO', 18)).toEqual({ valorUnitario: 2.97, impuesto: 0.53, tasa: 18 });
  });

  it('usa la tasa configurada de la empresa y la del IVAP', () => {
    expect(desglosarPrecio(110, 'GRAVADO_ONEROSO', 10)?.valorUnitario).toBe(100);
    expect(desglosarPrecio(104, 'GRAVADO_IVAP', 18)).toEqual({ valorUnitario: 100, impuesto: 4, tasa: 4 });
  });

  it('no aplica impuesto a operaciones exoneradas, inafectas o de exportación', () => {
    for (const tipo of ['EXONERADO_ONEROSO', 'INAFECTO_ONEROSO', 'EXPORTACION']) {
      expect(desglosarPrecio(50, tipo, 18)).toEqual({ valorUnitario: 50, impuesto: 0, tasa: 0 });
    }
  });

  it('no calcula nada sin un precio positivo', () => {
    expect(desglosarPrecio(null, 'GRAVADO_ONEROSO', 18)).toBeNull();
    expect(desglosarPrecio(0, 'GRAVADO_ONEROSO', 18)).toBeNull();
  });
});

describe('precioCompraSugerido', () => {
  it('propone el 70 % del precio de venta redondeado a céntimos', () => {
    expect(precioCompraSugerido(3.5)).toBe(2.45);
    expect(precioCompraSugerido(9.99)).toBe(6.99);
    expect(precioCompraSugerido(null)).toBeNull();
  });
});
