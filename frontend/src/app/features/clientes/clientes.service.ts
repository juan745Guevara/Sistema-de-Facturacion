import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TipoDocumentoIdentidad } from '../../core/api/catalogos-sunat.service';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface Cliente {
  id: number;
  tipoDocumento: TipoDocumentoIdentidad;
  numeroDocumento: string;
  nombre: string;
  direccion: string | null;
  email: string | null;
  telefono: string | null;
  fechaNacimiento: string | null;
}

export type DatosCliente = Omit<Cliente, 'id'>;

@Injectable({ providedIn: 'root' })
export class ClientesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/clientes`;

  buscar(consulta: ConsultaPaginada): Observable<Pagina<Cliente>> {
    return this.http.get<Pagina<Cliente>>(this.url, { params: parametros({ ...consulta }) });
  }

  crear(cliente: DatosCliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.url, cliente);
  }

  actualizar(id: number, cliente: DatosCliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.url}/${id}`, cliente);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
