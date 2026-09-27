import { FormControl, FormGroup } from '@angular/forms';

import { dniValidator, documentoSegunTipo, esDniValido, esRucValido, rucValidator } from './documento.validators';

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

describe('documentoSegunTipo', () => {
  const errorPara = (tipoDocumento: string, numeroDocumento: string) =>
    new FormGroup(
      { tipoDocumento: new FormControl(tipoDocumento), numeroDocumento: new FormControl(numeroDocumento) },
      { validators: documentoSegunTipo() },
    ).errors;

  it.each([
    ['RUC', '20601487871'],
    ['DNI', '47204426'],
    ['PASAPORTE', 'ab-123456'],
    ['SIN_DOCUMENTO', ''],
  ])('acepta %s %s', (tipo, numero) => {
    expect(errorPara(tipo, numero)).toBeNull();
  });

  it.each([
    ['RUC', '20601487872', 'ruc'],
    ['DNI', '1234567', 'dni'],
    ['CARNET_EXTRANJERIA', '', 'requerido'],
    ['PASAPORTE', 'AB 123', 'formato'],
  ])('rechaza %s "%s" con el error %s', (tipo, numero, error) => {
    expect(errorPara(tipo, numero)).toEqual({ documento: error });
  });
});
