import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface Cotizacion {
  id: number;
  serie: string;
  correlativo: number;
  fechaEmision: string;
  clienteNombre: string;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class CotizacionesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cotizaciones`;

  emitir(cuerpo: unknown): Observable<Cotizacion> {
    return this.http.post<Cotizacion>(this.url, cuerpo);
  }

  buscar(consulta: ConsultaPaginada): Observable<Pagina<Cotizacion>> {
    return this.http.get<Pagina<Cotizacion>>(this.url, { params: parametros({ ...consulta }) });
  }
}
