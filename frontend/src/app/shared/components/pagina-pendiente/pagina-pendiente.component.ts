import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Card } from 'primeng/card';

/** Página provisional para las rutas cuyo módulo todavía no se construye. */
@Component({
  selector: 'app-pagina-pendiente',
  imports: [Card],
  template: `
    <p-card [header]="titulo()">
      <p class="m-0 text-color-secondary">
        <i class="pi pi-wrench mr-2"></i>Esta pantalla se construye en la {{ fase() }}.
      </p>
    </p-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaPendienteComponent {
  readonly titulo = input.required<string>();
  readonly fase = input('siguiente fase');
}
