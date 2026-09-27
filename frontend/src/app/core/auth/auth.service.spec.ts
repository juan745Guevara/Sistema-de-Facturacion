import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { LoginResponse, Sesion } from './auth.models';
import { AuthService } from './auth.service';

const CLAVE = 'facturacion.sesion';

function sesion(expiraEn: Date, rol: Sesion['usuario']['rol'] = 'VENDEDOR'): Sesion {
  return {
    token: 'token-de-prueba',
    expiraEn: expiraEn.toISOString(),
    usuario: { id: 1, nombre: 'Ana Pérez', username: 'ana', rol },
  };
}

describe('AuthService', () => {
  function crear(): { auth: AuthService; http: HttpTestingController } {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    return { auth: TestBed.inject(AuthService), http: TestBed.inject(HttpTestingController) };
  }

  beforeEach(() => sessionStorage.clear());

  it('guarda la sesión al iniciar sesión', () => {
    const { auth, http } = crear();
    const respuesta: LoginResponse = { ...sesion(new Date(Date.now() + 60_000)), tipo: 'Bearer' };

    auth.login({ username: 'ana', password: 'secreta' }).subscribe();
    const peticion = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ username: 'ana', password: 'secreta' });
    peticion.flush(respuesta);

    expect(auth.estaAutenticado()).toBe(true);
    expect(auth.usuario()?.nombre).toBe('Ana Pérez');
    expect(auth.token()).toBe('token-de-prueba');
    expect(JSON.parse(sessionStorage.getItem(CLAVE)!)).not.toHaveProperty('tipo');
    http.verify();
  });

  it('restaura la sesión guardada', () => {
    sessionStorage.setItem(CLAVE, JSON.stringify(sesion(new Date(Date.now() + 60_000))));
    const { auth } = crear();

    expect(auth.estaAutenticado()).toBe(true);
    expect(auth.usuario()?.username).toBe('ana');
  });

  it('descarta la sesión vencida', () => {
    sessionStorage.setItem(CLAVE, JSON.stringify(sesion(new Date(Date.now() - 1000))));
    const { auth } = crear();

    expect(auth.estaAutenticado()).toBe(false);
    expect(auth.usuario()).toBeNull();
    expect(sessionStorage.getItem(CLAVE)).toBeNull();
  });

  it('descarta una sesión guardada corrupta', () => {
    sessionStorage.setItem(CLAVE, '{no es json');
    const { auth } = crear();

    expect(auth.estaAutenticado()).toBe(false);
    expect(sessionStorage.getItem(CLAVE)).toBeNull();
  });

  it('comprueba el rol del usuario', () => {
    sessionStorage.setItem(CLAVE, JSON.stringify(sesion(new Date(Date.now() + 60_000), 'ESPECIAL')));
    const { auth } = crear();

    expect(auth.tieneRol('ESPECIAL')).toBe(true);
    expect(auth.tieneRol('ADMINISTRADOR', 'ESPECIAL')).toBe(true);
    expect(auth.tieneRol('ADMINISTRADOR')).toBe(false);
  });

  it('cierra la sesión y vuelve al login', () => {
    sessionStorage.setItem(CLAVE, JSON.stringify(sesion(new Date(Date.now() + 60_000))));
    const { auth } = crear();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    auth.logout();

    expect(auth.estaAutenticado()).toBe(false);
    expect(sessionStorage.getItem(CLAVE)).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
