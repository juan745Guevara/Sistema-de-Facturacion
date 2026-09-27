import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { TipoDocumentoIdentidad } from '../../../core/api/catalogos-sunat.service';
import { ConsultaPaginada, Pagina, parametros } from '../../../core/api/pagina';

export interface Proveedor {
  id: number;
  tipoDocumento: TipoDocumentoIdentidad;
  numeroDocumento: string;
  nombre: string;
  direccion: string | null;
  email: string | null;
  telefono: string | null;
}

export type DatosProveedor = Omit<Proveedor, 'id'>;

@Injectable({ providedIn: 'root' })
export class ProveedoresService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/proveedores`;

  buscar(consulta: ConsultaPaginada): Observable<Pagina<Proveedor>> {
    return this.http.get<Pagina<Proveedor>>(this.url, { params: parametros({ ...consulta }) });
  }

  crear(proveedor: DatosProveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.url, proveedor);
  }

  actualizar(id: number, proveedor: DatosProveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.url}/${id}`, proveedor);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
