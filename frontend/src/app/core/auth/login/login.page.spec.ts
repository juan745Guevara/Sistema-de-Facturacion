import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Subject } from 'rxjs';

import { LoginResponse } from '../auth.models';
import { AuthService } from '../auth.service';
import { LoginPage } from './login.page';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let respuesta: Subject<LoginResponse>;
  const login = vi.fn();
  let navigateByUrl: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    respuesta = new Subject<LoginResponse>();
    login.mockReset().mockReturnValue(respuesta);
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), { provide: AuthService, useValue: { login } }],
    });
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
  });

  function escribir(selector: string, valor: string): void {
    const input = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  async function enviar(): Promise<void> {
    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  function texto(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('no llama al API si faltan datos y muestra los errores', async () => {
    await enviar();

    expect(login).not.toHaveBeenCalled();
    expect(texto()).toContain('Ingresa tu usuario');
    expect(texto()).toContain('Ingresa tu contraseña');
  });

  it('inicia sesión y va a la ruta pedida', async () => {
    fixture.componentRef.setInput('returnUrl', '/clientes');
    escribir('#username', 'ana');
    escribir('#password', 'secreta');
    await enviar();

    expect(login).toHaveBeenCalledWith({ username: 'ana', password: 'secreta' });
    respuesta.next({} as LoginResponse);
    expect(navigateByUrl).toHaveBeenCalledWith('/clientes');
  });

  it('ignora un returnUrl hacia otro sitio', async () => {
    fixture.componentRef.setInput('returnUrl', '//sitio-malicioso.com');
    escribir('#username', 'ana');
    escribir('#password', 'secreta');
    await enviar();
    respuesta.next({} as LoginResponse);

    expect(navigateByUrl).toHaveBeenCalledWith('/inicio');
  });

  it('muestra el detalle del error que devuelve el backend', async () => {
    escribir('#username', 'ana');
    escribir('#password', 'mala');
    await enviar();

    respuesta.error(
      new HttpErrorResponse({ status: 401, error: { detail: 'Usuario o contraseña incorrectos' } }),
    );
    await fixture.whenStable();

    expect(texto()).toContain('Usuario o contraseña incorrectos');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('avisa cuando el servidor no responde', async () => {
    escribir('#username', 'ana');
    escribir('#password', 'secreta');
    await enviar();

    respuesta.error(new HttpErrorResponse({ status: 0 }));
    await fixture.whenStable();

    expect(texto()).toContain('No se pudo conectar con el servidor');
  });
});
