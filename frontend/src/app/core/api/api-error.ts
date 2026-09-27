import { HttpErrorResponse } from '@angular/common/http';

/** Cuerpo ProblemDetail (RFC 9457) que devuelve el backend, con sus propiedades propias. */
export interface ProblemaApi {
  detail?: string;
  codigo?: string;
  errores?: { campo: string; mensaje: string }[];
}

export function problemaDe(error: unknown): ProblemaApi | null {
  return error instanceof HttpErrorResponse && error.error && typeof error.error === 'object'
    ? (error.error as ProblemaApi)
    : null;
}

export function codigoDeError(error: unknown): string | undefined {
  return problemaDe(error)?.codigo;
}

export function mensajeDeError(error: unknown, porDefecto = 'Ocurrió un error inesperado'): string {
  if (error instanceof HttpErrorResponse && error.status === 0) {
    return 'No se pudo conectar con el servidor';
  }
  const problema = problemaDe(error);
  if (problema?.errores?.length) {
    return problema.errores.map((e) => `${e.campo}: ${e.mensaje}`).join('; ');
  }
  return typeof problema?.detail === 'string' ? problema.detail : porDefecto;
}
