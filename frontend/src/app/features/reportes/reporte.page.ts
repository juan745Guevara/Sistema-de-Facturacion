import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';

import { Notificador } from '../../core/api/notificador.service';
import { ReportesService, ResumenReporte } from './reportes.service';

@Component({
  selector: 'app-reporte',
  imports: [FormsModule, Button, TableModule],
  template: `
    <h1 class="text-2xl font-semibold mt-0 mb-3">{{ tipo() === 'compras' ? 'Reporte de compras' : 'Reporte de ventas' }}</h1>
    <div class="flex flex-wrap gap-2 align-items-end mb-3">
      <div>
        <label class="block mb-1">Desde</label>
        <input class="p-inputtext" type="date" [(ngModel)]="desde" />
      </div>
      <div>
        <label class="block mb-1">Hasta</label>
        <input class="p-inputtext" type="date" [(ngModel)]="hasta" />
      </div>
      <p-button label="Consultar" (onClick)="consultar()" [loading]="cargando()" />
      <p-button label="Excel" [outlined]="true" (onClick)="bajar('xlsx')" />
      <p-button label="PDF" [outlined]="true" (onClick)="bajar('pdf')" />
    </div>
    @if (resumen(); as r) {
      <p class="text-color-secondary">{{ r.cantidad }} documentos · Gravadas {{ r.gravadas }} · IGV {{ r.igv }} · Total {{ r.total }}</p>
      <p-table [value]="r.filas">
        <ng-template #header>
          <tr><th>Tipo</th><th>Número</th><th>Fecha</th><th>Tercero</th><th>Total</th><th>Estado</th></tr>
        </ng-template>
        <ng-template #body let-fila>
          <tr>
            <td>{{ fila.tipo }}</td>
            <td>{{ fila.numero }}</td>
            <td>{{ fila.fecha }}</td>
            <td>{{ fila.tercero }}</td>
            <td>{{ fila.total }}</td>
            <td>{{ fila.estado }}</td>
          </tr>
        </ng-template>
      </p-table>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportePage implements OnInit {
  private readonly api = inject(ReportesService);
  private readonly notificador = inject(Notificador);

  readonly tipo = input.required<'ventas' | 'compras'>();
  protected desde = new Date().toISOString().slice(0, 8) + '01';
  protected hasta = new Date().toISOString().slice(0, 10);
  protected readonly resumen = signal<ResumenReporte | null>(null);
  protected readonly cargando = signal(false);

  ngOnInit(): void {
    this.consultar();
  }

  protected consultar(): void {
    this.cargando.set(true);
    this.api.resumen(this.tipo(), this.desde, this.hasta).subscribe({
      next: (r) => {
        this.resumen.set(r);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.notificador.error(e, 'No se pudo cargar el reporte');
      },
    });
  }

  protected bajar(formato: 'xlsx' | 'pdf'): void {
    this.api.descargar(this.tipo(), formato, this.desde, this.hasta).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte-${this.tipo()}.${formato}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (e: unknown) => this.notificador.error(e, 'No se pudo descargar'),
    });
  }
}
