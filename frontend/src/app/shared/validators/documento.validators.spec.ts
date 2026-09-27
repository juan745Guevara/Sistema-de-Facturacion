import { FormControl } from '@angular/forms';

import { dniValidator, esDniValido, esRucValido, rucValidator } from './documento.validators';

describe('validadores de documento', () => {
  it.each(['20601487871', '10472044261', '20100070970'])('acepta el RUC %s', (ruc) => {
    expect(esRucValido(ruc)).toBe(true);
  });

  it.each([
    ['20601487872', 'dígito verificador incorrecto'],
    ['30601487871', 'prefijo inexistente'],
    ['2060148787', 'solo 10 dígitos'],
    ['2060148787A', 'contiene letras'],
  ])('rechaza el RUC %s (%s)', (ruc) => {
    expect(esRucValido(ruc)).toBe(false);
  });

  it('valida el DNI por longitud y dígitos', () => {
    expect(esDniValido('47204426')).toBe(true);
    expect(esDniValido('4720442')).toBe(false);
    expect(esDniValido('4720442X')).toBe(false);
  });

  it('deja los campos vacíos a Validators.required', () => {
    expect(new FormControl('', rucValidator).errors).toBeNull();
    expect(new FormControl(null, dniValidator).errors).toBeNull();
  });

  it('marca el error correspondiente en el control', () => {
    expect(new FormControl('123', rucValidator).errors).toEqual({ ruc: true });
    expect(new FormControl('123', dniValidator).errors).toEqual({ dni: true });
    expect(new FormControl(' 20601487871 ', rucValidator).errors).toBeNull();
  });
});
