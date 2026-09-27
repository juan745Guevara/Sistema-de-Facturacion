import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Card } from 'primeng/card';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-inicio',
  imports: [Card],
  template: `
    <h1 class="mt-0 text-2xl">Hola, {{ usuario()?.nombre }}</h1>
    <p-card>
      <p class="m-0 text-color-secondary">
        El panel con ventas del día, comprobantes pendientes de envío y productos más vendidos llega en la Fase 6.
      </p>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InicioPage {
  protected readonly usuario = inject(AuthService).usuario;
}
