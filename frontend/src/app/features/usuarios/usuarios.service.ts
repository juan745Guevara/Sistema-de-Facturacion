import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Rol } from '../../core/auth/auth.models';

export interface Usuario {
  id: number;
  nombre: string;
  username: string;
  email: string | null;
  rol: Rol;
  activo: boolean;
  ultimoLogin: string | null;
}

export interface NuevoUsuario {
  nombre: string;
  username: string;
  email: string | null;
  rol: Rol;
  password: string;
}

export interface CambiosUsuario {
  nombre: string;
  email: string | null;
  rol: Rol;
  activo: boolean;
}

/** Igual que `Usuario.LONGITUD_MINIMA_PASSWORD` del backend. */
export const LONGITUD_MINIMA_PASSWORD = 12;

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  crear(usuario: NuevoUsuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, usuario);
  }

  actualizar(id: number, cambios: CambiosUsuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.url}/${id}`, cambios);
  }

  cambiarPassword(id: number, password: string): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/password`, { password });
  }
}
