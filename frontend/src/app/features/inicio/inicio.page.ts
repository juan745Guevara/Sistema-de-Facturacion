import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Card } from 'primeng/card';

import { Notificador } from '../../core/api/notificador.service';
import { AuthService } from '../../core/auth/auth.service';
import { Dashboard, ReportesService } from '../reportes/reportes.service';

@Component({
  selector: 'app-inicio',
  imports: [Card],
  template: `
    <h1 class="mt-0 text-2xl">Hola, {{ usuario()?.nombre }}</h1>
    <div class="grid">
      <div class="col-12 md:col-3">
        <p-card header="Ventas de hoy">
          <p class="text-2xl font-semibold m-0">{{ dashboard()?.ventasHoy ?? '—' }}</p>
        </p-card>
      </div>
      <div class="col-12 md:col-3">
        <p-card header="Pendientes SUNAT">
          <p class="text-2xl font-semibold m-0">{{ dashboard()?.comprobantesPendientes ?? '—' }}</p>
        </p-card>
      </div>
      <div class="col-12 md:col-3">
        <p-card header="Compras del mes">
          <p class="text-2xl font-semibold m-0">{{ dashboard()?.comprasDelMes ?? '—' }}</p>
        </p-card>
      </div>
      <div class="col-12 md:col-3">
        <p-card header="Total compras mes">
          <p class="text-2xl font-semibold m-0">{{ dashboard()?.totalComprasMes ?? '—' }}</p>
        </p-card>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InicioPage implements OnInit {
  protected readonly usuario = inject(AuthService).usuario;
  private readonly reportes = inject(ReportesService);
  private readonly notificador = inject(Notificador);
  protected readonly dashboard = signal<Dashboard | null>(null);

  ngOnInit(): void {
    this.reportes.dashboard().subscribe({
      next: (d) => this.dashboard.set(d),
      error: (e: unknown) => this.notificador.error(e, 'No se pudo cargar el panel'),
    });
  }
}
