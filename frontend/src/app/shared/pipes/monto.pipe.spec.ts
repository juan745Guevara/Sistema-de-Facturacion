import { MontoPipe } from './monto.pipe';

describe('MontoPipe', () => {
  const pipe = new MontoPipe();

  it('formatea soles con dos decimales y separador de miles', () => {
    expect(pipe.transform(1234.5)).toBe('S/ 1,234.50');
    expect(pipe.transform('63')).toBe('S/ 63.00');
  });

  it('usa el símbolo de dólares', () => {
    expect(pipe.transform(10, 'USD')).toBe('US$ 10.00');
  });

  it('devuelve vacío si no hay un número', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
    expect(pipe.transform('')).toBe('');
    expect(pipe.transform('abc')).toBe('');
  });
});
