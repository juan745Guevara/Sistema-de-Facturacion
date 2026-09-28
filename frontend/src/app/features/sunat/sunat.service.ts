import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface DocumentoSunat {
  id: number;
  tipo: string;
  serie: string;
  correlativo: number;
  fechaEmision: string;
  estado: string;
  codigoRespuesta: string | null;
  mensaje: string | null;
  hash: string | null;
  intentos: number;
  tieneXml: boolean;
  tieneCdr: boolean;
}

@Injectable({ providedIn: 'root' })
export class SunatService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/sunat/documentos`;

  buscar(consulta: ConsultaPaginada & { tipo?: string; estado?: string }): Observable<Pagina<DocumentoSunat>> {
    return this.http.get<Pagina<DocumentoSunat>>(this.url, { params: parametros({ ...consulta }) });
  }

  obtener(tipo: string, serie: string, correlativo: number): Observable<DocumentoSunat> {
    return this.http.get<DocumentoSunat>(`${this.url}/${tipo}/${serie}/${correlativo}`);
  }

  reenviar(tipo: string, serie: string, correlativo: number): Observable<DocumentoSunat> {
    return this.http.post<DocumentoSunat>(`${this.url}/${tipo}/${serie}/${correlativo}/enviar`, {});
  }

  previsualizarResumen(fecha: string): Observable<DocumentoPendiente[]> {
    return this.http.get<DocumentoPendiente[]>(`${environment.apiUrl}/sunat/lotes/resumen/previsualizar`, {
      params: parametros({ fecha }),
    });
  }

  generarResumen(fecha: string): Observable<LoteSunat> {
    return this.http.post<LoteSunat>(`${environment.apiUrl}/sunat/lotes/resumen`, {}, { params: parametros({ fecha }) });
  }

  darBaja(tipo: string, serie: string, correlativo: number, motivo: string): Observable<LoteSunat> {
    return this.http.post<LoteSunat>(`${environment.apiUrl}/sunat/lotes/baja`, { tipo, serie, correlativo, motivo });
  }

  consultarTicket(id: number): Observable<LoteSunat> {
    return this.http.post<LoteSunat>(`${environment.apiUrl}/sunat/lotes/${id}/ticket`, {});
  }

  buscarLotes(consulta: ConsultaPaginada & { tipo?: string }): Observable<Pagina<LoteSunat>> {
    return this.http.get<Pagina<LoteSunat>>(`${environment.apiUrl}/sunat/lotes`, { params: parametros({ ...consulta }) });
  }
}

export interface DocumentoPendiente {
  tipo: string;
  serie: string;
  correlativo: number;
  cliente: string;
  total: string;
}

export interface LoteSunat {
  id: number;
  tipo: string;
  serie: string;
  correlativo: number;
  fechaReferencia: string;
  estado: string;
  ticket: string | null;
  mensaje: string | null;
}
