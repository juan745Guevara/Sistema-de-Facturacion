import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const PESOS_RUC = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
const PREFIJOS_RUC = ['10', '15', '16', '17', '20'];

/** RUC de 11 dígitos con prefijo válido y dígito verificador módulo 11 de SUNAT. */
export function esRucValido(valor: string): boolean {
  if (!/^\d{11}$/.test(valor) || !PREFIJOS_RUC.includes(valor.slice(0, 2))) {
    return false;
  }
  const suma = PESOS_RUC.reduce((acc, peso, i) => acc + peso * Number(valor[i]), 0);
  const digito = (11 - (suma % 11)) % 10;
  return digito === Number(valor[10]);
}

export function esDniValido(valor: string): boolean {
  return /^\d{8}$/.test(valor);
}

export const rucValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  vacio(control.value) || esRucValido(String(control.value).trim()) ? null : { ruc: true };

export const dniValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  vacio(control.value) || esDniValido(String(control.value).trim()) ? null : { dni: true };

function vacio(valor: unknown): boolean {
  return valor === null || valor === undefined || String(valor).trim() === '';
}
