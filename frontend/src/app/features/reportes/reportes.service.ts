import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { parametros } from '../../core/api/pagina';

export interface FilaReporte {
  tipo: string;
  numero: string;
  fecha: string;
  tercero: string;
  total: number;
  estado: string;
}

export interface ResumenReporte {
  desde: string;
  hasta: string;
  cantidad: number;
  gravadas: number;
  igv: number;
  total: number;
  filas: FilaReporte[];
}

export interface Dashboard {
  ventasHoy: number;
  comprobantesPendientes: number;
  comprasDelMes: number;
  totalComprasMes: number;
}

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/reportes`;

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.url}/dashboard`);
  }

  resumen(tipo: 'ventas' | 'compras', desde: string, hasta: string): Observable<ResumenReporte> {
    return this.http.get<ResumenReporte>(`${this.url}/${tipo}`, { params: parametros({ desde, hasta }) });
  }

  descargar(tipo: 'ventas' | 'compras', formato: 'xlsx' | 'pdf', desde: string, hasta: string): Observable<Blob> {
    return this.http.get(`${this.url}/${tipo}.${formato}`, {
      params: parametros({ desde, hasta }),
      responseType: 'blob',
    });
  }

  enviarCorreo(destinatario: string, tipo: 'ventas' | 'compras', desde: string, hasta: string): Observable<void> {
    return this.http.post<void>(`${this.url}/correo`, null, {
      params: parametros({ destinatario, tipo, desde, hasta }),
    });
  }
}
