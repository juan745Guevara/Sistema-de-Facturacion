import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvironmentProviders, Provider } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';

import { Rol } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

/** Proveedores comunes de las pruebas de páginas, con una sesión del rol indicado. */
export function proveedoresDePagina(rol: Rol): (Provider | EnvironmentProviders)[] {
  return [
    provideRouter([]),
    provideHttpClient(),
    provideHttpClientTesting(),
    MessageService,
    ConfirmationService,
    {
      provide: AuthService,
      useValue: {
        usuario: () => ({ id: 1, nombre: 'Prueba', username: 'prueba', rol }),
        tieneRol: (...roles: Rol[]) => roles.includes(rol),
      },
    },
  ];
}

export function elemento<T extends HTMLElement = HTMLElement>(fixture: ComponentFixture<unknown>, selector: string): T | null {
  return (fixture.nativeElement as HTMLElement).querySelector<T>(selector);
}

export function boton(fixture: ComponentFixture<unknown>, etiqueta: string): HTMLButtonElement | null {
  return elemento<HTMLButtonElement>(fixture, `button[aria-label="${etiqueta}"]`) ??
    Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === etiqueta,
    ) ??
    null;
}

export function escribir(fixture: ComponentFixture<unknown>, selector: string, valor: string): void {
  const input = elemento<HTMLInputElement>(fixture, selector);
  if (!input) {
    throw new Error(`No existe ${selector}`);
  }
  input.value = valor;
  input.dispatchEvent(new Event('input'));
}

export async function enviar(fixture: ComponentFixture<unknown>, selector = 'form'): Promise<void> {
  elemento(fixture, selector)?.dispatchEvent(new Event('submit'));
  await fixture.whenStable();
}
