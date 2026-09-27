import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TipoDocumentoIdentidad } from './catalogos-sunat.service';

export interface DatosPadron {
  tipoDocumento: TipoDocumentoIdentidad;
  numeroDocumento: string;
  nombre: string;
  direccion: string | null;
  ubigeo: string | null;
  departamento: string | null;
  provincia: string | null;
  distrito: string | null;
  estado: string | null;
  condicion: string | null;
}

/** Consulta de RUC (SUNAT) y DNI (RENIEC) a través del backend. */
@Injectable({ providedIn: 'root' })
export class PadronService {
  private readonly http = inject(HttpClient);

  consultar(tipo: 'DNI' | 'RUC', numero: string): Observable<DatosPadron> {
    return this.http.get<DatosPadron>(
      `${environment.apiUrl}/documentos-identidad/${tipo}/${encodeURIComponent(numero)}`,
    );
  }
}
