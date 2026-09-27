import { HttpParams } from '@angular/common/http';

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
}

export interface ConsultaPaginada {
  q?: string;
  pagina: number;
  tamanio: number;
}

/** Omite los filtros vacíos para no enviar `q=` ni `categoriaId=null`. */
export function parametros(valores: Record<string, string | number | boolean | null | undefined>): HttpParams {
  return Object.entries(valores).reduce(
    (params, [clave, valor]) =>
      valor === null || valor === undefined || valor === '' ? params : params.set(clave, String(valor)),
    new HttpParams(),
  );
}
