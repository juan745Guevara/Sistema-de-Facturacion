import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Password } from 'primeng/password';

import { mensajeDeError } from '../../api/api-error';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, Button, Card, InputText, Message, Password],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly returnUrl = input<string>();

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.maxLength(100)]],
  });

  protected ingresar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.auth.login(this.formulario.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl(this.destinoSeguro()),
      error: (e: unknown) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, 'No se pudo iniciar sesión'));
      },
    });
  }

  /** Solo rutas internas, para no redirigir a otro sitio con un returnUrl manipulado. */
  private destinoSeguro(): string {
    const destino = this.returnUrl();
    return destino?.startsWith('/') && !destino.startsWith('//') ? destino : '/inicio';
  }
}
