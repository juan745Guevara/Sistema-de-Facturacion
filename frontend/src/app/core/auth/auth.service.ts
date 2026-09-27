import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Rol, Sesion } from './auth.models';

const CLAVE_SESION = 'facturacion.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<Sesion | null>(leerSesionGuardada());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly token = computed(() => this.sesion()?.token ?? null);

  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credenciales).pipe(
      tap(({ token, expiraEn, usuario }) => this.guardar({ token, expiraEn, usuario })),
    );
  }

  logout(): void {
    sessionStorage.removeItem(CLAVE_SESION);
    this.sesion.set(null);
    void this.router.navigate(['/login']);
  }

  /** Se evalúa en cada llamada porque la expiración depende de la hora actual. */
  estaAutenticado(): boolean {
    const sesion = this.sesion();
    if (!sesion) {
      return false;
    }
    if (Date.parse(sesion.expiraEn) <= Date.now()) {
      sessionStorage.removeItem(CLAVE_SESION);
      this.sesion.set(null);
      return false;
    }
    return true;
  }

  tieneRol(...roles: Rol[]): boolean {
    const rol = this.usuario()?.rol;
    return rol !== undefined && roles.includes(rol);
  }

  private guardar(sesion: Sesion): void {
    sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this.sesion.set(sesion);
  }
}

function leerSesionGuardada(): Sesion | null {
  const guardada = sessionStorage.getItem(CLAVE_SESION);
  if (!guardada) {
    return null;
  }
  try {
    return JSON.parse(guardada) as Sesion;
  } catch {
    sessionStorage.removeItem(CLAVE_SESION);
    return null;
  }
}
