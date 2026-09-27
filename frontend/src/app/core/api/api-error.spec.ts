import { HttpErrorResponse } from '@angular/common/http';

import { codigoDeError, mensajeDeError } from './api-error';

describe('mensajeDeError', () => {
  it('usa el detalle del ProblemDetail', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { detail: 'Ya existe un cliente con el documento RUC 20601487871', codigo: 'cliente-duplicado' },
    });

    expect(mensajeDeError(error)).toBe('Ya existe un cliente con el documento RUC 20601487871');
    expect(codigoDeError(error)).toBe('cliente-duplicado');
  });

  it('lista los errores de validación por campo', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Datos inválidos', errores: [{ campo: 'ruc', mensaje: 'debe tener 11 dígitos' }] },
    });

    expect(mensajeDeError(error)).toBe('ruc: debe tener 11 dígitos');
  });

  it('distingue la falta de conexión y usa el mensaje por defecto en otros casos', () => {
    expect(mensajeDeError(new HttpErrorResponse({ status: 0 }))).toBe('No se pudo conectar con el servidor');
    expect(mensajeDeError(new Error('x'), 'No se pudo guardar')).toBe('No se pudo guardar');
    expect(codigoDeError(new Error('x'))).toBeUndefined();
  });
});
