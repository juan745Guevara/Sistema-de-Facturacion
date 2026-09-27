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

export type ErrorDocumento = 'requerido' | 'ruc' | 'dni' | 'formato';

/**
 * Valida el número según el tipo elegido en el mismo grupo, con las reglas del backend.
 * El error queda en el grupo como `{ documento: ErrorDocumento }`.
 */
export function documentoSegunTipo(campoTipo = 'tipoDocumento', campoNumero = 'numeroDocumento'): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const tipo = grupo.get(campoTipo)?.value as string | null;
    const numero = String(grupo.get(campoNumero)?.value ?? '').trim().toUpperCase();
    const error = errorDocumento(tipo, numero);
    return error ? { documento: error } : null;
  };
}

function errorDocumento(tipo: string | null, numero: string): ErrorDocumento | null {
  if (!tipo || tipo === 'SIN_DOCUMENTO') {
    return null;
  }
  if (numero === '') {
    return 'requerido';
  }
  if (tipo === 'RUC') {
    return esRucValido(numero) ? null : 'ruc';
  }
  if (tipo === 'DNI') {
    return esDniValido(numero) ? null : 'dni';
  }
  return /^[A-Z0-9-]{1,15}$/.test(numero) ? null : 'formato';
}

export const MENSAJES_DOCUMENTO: Record<ErrorDocumento, string> = {
  requerido: 'Ingresa el número de documento',
  ruc: 'El RUC no es válido',
  dni: 'El DNI debe tener 8 dígitos',
  formato: 'Solo letras, números y guiones (máx. 15)',
};

function vacio(valor: unknown): boolean {
  return valor === null || valor === undefined || String(valor).trim() === '';
}
