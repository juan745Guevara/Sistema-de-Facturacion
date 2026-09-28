import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';

import { Notificador } from '../../../core/api/notificador.service';
import { ListadoPaginado } from '../../../shared/utils/listado-paginado';
import { DocumentoPendiente, LoteSunat, SunatService } from '../sunat.service';

@Component({
  selector: 'app-resumen-diario',
  imports: [FormsModule, Button, DatePicker, TableModule, Tag],
  templateUrl: './resumen-diario.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResumenDiarioPage {
  private readonly sunat = inject(SunatService);
  private readonly notificador = inject(Notificador);

  protected fecha = new Date();
  protected readonly pendientes = signal<DocumentoPendiente[]>([]);
  protected readonly generando = signal(false);

  protected readonly listado = new ListadoPaginado<LoteSunat>(
    (consulta) => this.sunat.buscarLotes(consulta),
    (e) => this.notificador.error(e, 'No se pudieron cargar los lotes'),
  );

  protected cargarPendientes(): void {
    this.sunat.previsualizarResumen(this.iso(this.fecha)).subscribe({
      next: (docs) => this.pendientes.set(docs),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar las boletas'),
    });
  }

  protected generar(): void {
    this.generando.set(true);
    this.sunat.generarResumen(this.iso(this.fecha)).subscribe({
      next: (lote) => {
        this.generando.set(false);
        this.pendientes.set([]);
        this.notificador.exito(`Resumen ${lote.serie}-${lote.correlativo}: ${lote.estado}`);
        this.listado.recargar();
      },
      error: (e: unknown) => {
        this.generando.set(false);
        this.notificador.error(e, 'No se pudo generar el resumen');
      },
    });
  }

  protected consultar(lote: LoteSunat): void {
    this.sunat.consultarTicket(lote.id).subscribe({
      next: (actualizado) => {
        this.notificador.exito(`${actualizado.serie}-${actualizado.correlativo}: ${actualizado.estado}`);
        this.listado.recargar();
      },
      error: (e: unknown) => this.notificador.error(e, 'No se pudo consultar el ticket'),
    });
  }

  protected severidad(estado: string): 'success' | 'warn' | 'danger' | 'secondary' {
    return estado === 'ACEPTADO' ? 'success' : estado === 'RECHAZADO' ? 'danger' : 'secondary';
  }

  private iso(fecha: Date): string {
    return fecha.toISOString().slice(0, 10);
  }
}
