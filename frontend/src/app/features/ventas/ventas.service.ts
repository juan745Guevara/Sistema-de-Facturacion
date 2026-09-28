import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export type TipoVenta = 'FACTURA' | 'BOLETA' | 'NOTA_VENTA';

export interface ItemVentaRequest {
  productoId: number;
  cantidad: number;
  precioUnitario?: number | null;
  descuento?: number | null;
  icbper?: boolean;
}

export interface EmisionRequest {
  tipo: TipoVenta;
  serie: string;
  clienteId: number;
  moneda: 'PEN' | 'USD';
  tipoCambio?: number | null;
  fechaVencimiento?: string | null;
  descuentoGlobal?: { tipo: 'MONTO' | 'PORCENTAJE'; valor: number } | null;
  formaPago: 'CONTADO' | 'CREDITO';
  cuotas?: { fechaPago: string; monto: number }[];
  items: ItemVentaRequest[];
  observacion?: string | null;
}

export interface Totales {
  gravadas: number;
  exoneradas: number;
  inafectas: number;
  gratuitas: number;
  descuentoGlobal: number;
  igv: number;
  icbper: number;
  valorVenta: number;
  total: number;
}

export interface Venta {
  id: number;
  tipo: TipoVenta;
  serie: string;
  correlativo: number;
  fechaEmision: string;
  clienteNombre: string;
  clienteNumeroDocumento: string;
  totales: Totales;
  estadoSunat: string;
  lineas: { codigo: string; descripcion: string; cantidad: number; importe: number }[];
}

export interface Serie {
  id: number;
  tipo: string;
  serie: string;
  correlativo: number;
  activa: boolean;
}

export interface Calculo {
  lineas: { valorUnitario: number; impuesto: number; valorVenta: number; icbper: number; importe: number }[];
  totales: Totales;
}

@Injectable({ providedIn: 'root' })
export class VentasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/ventas`;

  previsualizar(items: ItemVentaRequest[], moneda: 'PEN' | 'USD' = 'PEN', tipoCambio?: number | null): Observable<Calculo> {
    return this.http.post<Calculo>(`${this.url}/previsualizar`, { items, moneda, tipoCambio });
  }

  emitir(solicitud: EmisionRequest): Observable<Venta> {
    return this.http.post<Venta>(this.url, solicitud);
  }

  buscar(consulta: ConsultaPaginada & { tipo?: TipoVenta }): Observable<Pagina<Venta>> {
    return this.http.get<Pagina<Venta>>(this.url, { params: parametros({ ...consulta }) });
  }

  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.url}/${id}`);
  }

  series(tipo: TipoVenta): Observable<Serie[]> {
    return this.http.get<Serie[]>(`${environment.apiUrl}/series`, { params: parametros({ tipo }) });
  }
}
