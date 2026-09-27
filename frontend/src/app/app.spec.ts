import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { appConfig } from './app.config';
import { Sesion } from './core/auth/auth.models';

function iniciarSesionComo(rol: Sesion['usuario']['rol']): void {
  const sesion: Sesion = {
    token: 'token',
    expiraEn: new Date(Date.now() + 60_000).toISOString(),
    usuario: { id: 1, nombre: 'Ana', username: 'ana', rol },
  };
  sessionStorage.setItem('facturacion.sesion', JSON.stringify(sesion));
}

describe('rutas de la aplicación', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, provideHttpClientTesting()],
    });
  });

  it('manda al login a quien no inició sesión, recordando a dónde iba', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/clientes');

    expect(TestBed.inject(Router).url).toBe('/login?returnUrl=%2Fclientes');
  });

  it('con sesión, el login redirige al inicio', async () => {
    iniciarSesionComo('VENDEDOR');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/login');

    expect(TestBed.inject(Router).url).toBe('/inicio');
  });

  it('impide a un vendedor entrar a la configuración de la empresa', async () => {
    iniciarSesionComo('VENDEDOR');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/empresa');

    expect(TestBed.inject(Router).url).toBe('/inicio');
  });

  it('permite al administrador entrar a la configuración de la empresa', async () => {
    iniciarSesionComo('ADMINISTRADOR');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/empresa');
    await harness.fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/empresa');
    expect(harness.routeNativeElement?.textContent).toContain('Datos de la empresa');
    TestBed.inject(HttpTestingController).expectOne((r) => r.url.endsWith('/api/empresa'));
  });

  it('redirige secciones con subrutas a su pantalla principal', async () => {
    iniciarSesionComo('VENDEDOR');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/ventas');

    expect(TestBed.inject(Router).url).toBe('/ventas/factura');
  });
});
