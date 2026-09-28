import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface Compra {
  id: number;
  tipo: string;
  serie: string;
  correlativo: string;
  fechaEmision: string;
  proveedorNombre: string;
  total: number;
  anulada: boolean;
}

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/compras`;

  registrar(cuerpo: unknown): Observable<Compra> {
    return this.http.post<Compra>(this.url, cuerpo);
  }

  buscar(consulta: ConsultaPaginada): Observable<Pagina<Compra>> {
    return this.http.get<Pagina<Compra>>(this.url, { params: parametros({ ...consulta }) });
  }

  anular(id: number): Observable<Compra> {
    return this.http.post<Compra>(`${this.url}/${id}/anular`, {});
  }
}
