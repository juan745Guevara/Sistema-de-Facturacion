import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';

import { Notificador } from '../../../core/api/notificador.service';
import { ListadoPaginado } from '../../../shared/utils/listado-paginado';
import { Cotizacion, CotizacionesService } from '../cotizaciones.service';

@Component({
  selector: 'app-cotizaciones',
  imports: [RouterLink, Button, TableModule],
  template: `
    <div class="flex justify-content-between align-items-center mb-3">
      <h1 class="text-2xl font-semibold m-0">Cotizaciones</h1>
      <p-button label="Nueva" icon="pi pi-plus" routerLink="/cotizaciones/nueva" />
    </div>
    <p-table
      [value]="listado.filas()"
      [lazy]="true"
      (onLazyLoad)="listado.cambiarPagina($event)"
      [paginator]="true"
      [first]="listado.primero()"
      [rows]="listado.tamanio()"
      [totalRecords]="listado.total()"
      [loading]="listado.cargando()"
    >
      <ng-template #header>
        <tr><th>Número</th><th>Fecha</th><th>Cliente</th><th>Total</th></tr>
      </ng-template>
      <ng-template #body let-item>
        <tr>
          <td>{{ item.serie }}-{{ item.correlativo }}</td>
          <td>{{ item.fechaEmision }}</td>
          <td>{{ item.clienteNombre }}</td>
          <td>{{ item.total }}</td>
        </tr>
      </ng-template>
    </p-table>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CotizacionesPage {
  private readonly api = inject(CotizacionesService);
  private readonly notificador = inject(Notificador);
  protected readonly listado = new ListadoPaginado<Cotizacion>(
    (consulta) => this.api.buscar(consulta),
    (e) => this.notificador.error(e, 'No se pudieron cargar las cotizaciones'),
  );
}
