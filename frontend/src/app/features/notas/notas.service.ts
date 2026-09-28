import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';
import { Calculo, ItemVentaRequest, Totales } from '../ventas/ventas.service';

export type TipoNota = 'NOTA_CREDITO' | 'NOTA_DEBITO';
export type TipoReferencia = 'FACTURA' | 'BOLETA';

export interface EmisionNotaRequest {
  tipo: TipoNota;
  serie: string;
  tipoReferencia: TipoReferencia;
  serieReferencia: string;
  correlativoReferencia: number;
  codigoMotivo: string;
  items?: ItemVentaRequest[];
  observacion?: string | null;
}

export interface Nota {
  id: number;
  tipo: TipoNota;
  serie: string;
  correlativo: number;
  fechaEmision: string;
  clienteNombre: string;
  tipoReferencia: TipoReferencia;
  serieReferencia: string;
  correlativoReferencia: number;
  codigoMotivo: string;
  descripcionMotivo: string;
  totales: Totales;
  estadoSunat: string;
}

@Injectable({ providedIn: 'root' })
export class NotasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/notas`;

  previsualizar(solicitud: Omit<EmisionNotaRequest, 'tipo' | 'serie'>): Observable<Calculo> {
    return this.http.post<Calculo>(`${this.url}/previsualizar`, solicitud);
  }

  emitir(solicitud: EmisionNotaRequest): Observable<Nota> {
    return this.http.post<Nota>(this.url, solicitud);
  }

  buscar(consulta: ConsultaPaginada & { tipo?: TipoNota }): Observable<Pagina<Nota>> {
    return this.http.get<Pagina<Nota>>(this.url, { params: parametros({ ...consulta }) });
  }
}
