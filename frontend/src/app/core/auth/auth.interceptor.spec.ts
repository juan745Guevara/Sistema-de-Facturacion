import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  const token = signal<string | null>(null);
  const logout = vi.fn();
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    token.set(null);
    logout.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { token, logout } },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('agrega el token a las llamadas al API', () => {
    token.set('abc');
    http.get(`${environment.apiUrl}/clientes`).subscribe();

    const peticion = backend.expectOne(`${environment.apiUrl}/clientes`);
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer abc');
    peticion.flush([]);
  });

  it('no envía el token a otros dominios', () => {
    token.set('abc');
    http.get('https://api.externa.pe/tipo-cambio').subscribe();

    const peticion = backend.expectOne('https://api.externa.pe/tipo-cambio');
    expect(peticion.request.headers.has('Authorization')).toBe(false);
    peticion.flush({});
  });

  it('cierra la sesión si el API rechaza el token', () => {
    token.set('vencido');
    http.get(`${environment.apiUrl}/clientes`).subscribe({ error: () => undefined });

    backend.expectOne(`${environment.apiUrl}/clientes`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(logout).toHaveBeenCalledOnce();
  });

  it('no cierra sesión por un 401 sin token, como un login fallido', () => {
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe({ error: () => undefined });

    backend.expectOne(`${environment.apiUrl}/auth/login`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(logout).not.toHaveBeenCalled();
  });
});
