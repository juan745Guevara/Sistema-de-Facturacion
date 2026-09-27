import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface Direccion {
  direccion: string;
  ubigeo: string;
  departamento: string;
  provincia: string;
  distrito: string;
  codigoPais: string | null;
  codigoEstablecimiento: string | null;
}

export interface Empresa {
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  domicilioFiscal: Direccion;
  telefono: string | null;
  correoVentas: string | null;
  correoSoporte: string | null;
  porcentajeIgv: number;
  bienesSelva: boolean;
  serviciosSelva: boolean;
}

export const IGV_POR_DEFECTO = 18;

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/empresa`;

  /** Emite `null` mientras la empresa no esté configurada. */
  obtener(): Observable<Empresa | null> {
    return this.http.get<Empresa>(this.url).pipe(
      catchError((e: unknown) =>
        e instanceof HttpErrorResponse && e.status === 404 ? of(null) : throwError(() => e),
      ),
    );
  }

  guardar(empresa: Empresa): Observable<Empresa> {
    return this.http.put<Empresa>(this.url, empresa);
  }
}
