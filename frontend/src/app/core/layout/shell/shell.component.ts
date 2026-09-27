import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { PanelMenu } from 'primeng/panelmenu';
import { Toolbar } from 'primeng/toolbar';

import { AuthService } from '../../auth/auth.service';
import { menuPara } from '../menu';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Avatar, Button, PanelMenu, Toolbar],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShellComponent {
  private readonly auth = inject(AuthService);

  protected readonly usuario = this.auth.usuario;
  protected readonly menu = computed(() => menuPara(this.usuario()?.rol));
  protected readonly menuAbierto = signal(esPantallaAncha());
  protected readonly inicial = computed(() => this.usuario()?.nombre.charAt(0).toUpperCase() ?? '?');

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected salir(): void {
    this.auth.logout();
  }
}

function esPantallaAncha(): boolean {
  return typeof window.matchMedia !== 'function' || window.matchMedia('(min-width: 769px)').matches;
}
