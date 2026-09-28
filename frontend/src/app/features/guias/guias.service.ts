import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface Guia {
  id: number;
  serie: string;
  correlativo: number;
  fechaEmision: string;
  destinatarioNombre: string;
  estadoSunat: string;
}

export interface Ubigeo {
  codigo: string;
  departamento: string;
  provincia: string;
  distrito: string;
}

@Injectable({ providedIn: 'root' })
export class GuiasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/guias`;

  emitir(cuerpo: unknown): Observable<Guia> {
    return this.http.post<Guia>(this.url, cuerpo);
  }

  buscar(consulta: ConsultaPaginada): Observable<Pagina<Guia>> {
    return this.http.get<Pagina<Guia>>(this.url, { params: parametros({ ...consulta }) });
  }

  ubigeos(q: string): Observable<Ubigeo[]> {
    return this.http.get<Ubigeo[]>(`${environment.apiUrl}/ubigeos`, { params: parametros({ q }) });
  }
}
