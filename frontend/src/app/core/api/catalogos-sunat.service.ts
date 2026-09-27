import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { environment } from '../../../environments/environment';

export type TipoDocumentoIdentidad =
  | 'SIN_DOCUMENTO'
  | 'DNI'
  | 'CARNET_EXTRANJERIA'
  | 'RUC'
  | 'PASAPORTE'
  | 'CEDULA_DIPLOMATICA'
  | 'DOC_PAIS_RESIDENCIA'
  | 'TIN'
  | 'IN'
  | 'TAM';

/** `valor` es el nombre que usa la API; `codigo` es el del catálogo SUNAT. */
export interface OpcionCatalogo {
  valor: string;
  codigo: string;
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogosSunatService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/catalogos-sunat`;

  readonly tiposDocumentoIdentidad$: Observable<OpcionCatalogo[]> = this.http
    .get<OpcionCatalogo[]>(`${this.url}/tipos-documento-identidad`)
    .pipe(shareReplay(1));

  readonly tiposAfectacionIgv$: Observable<OpcionCatalogo[]> = this.http
    .get<OpcionCatalogo[]>(`${this.url}/tipos-afectacion-igv`)
    .pipe(shareReplay(1));
}
