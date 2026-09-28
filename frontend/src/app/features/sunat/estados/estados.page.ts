import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';

import { Notificador } from '../../../core/api/notificador.service';
import { ListadoPaginado } from '../../../shared/utils/listado-paginado';
import { DocumentoSunat, SunatService } from '../sunat.service';

@Component({
  selector: 'app-estados-sunat',
  imports: [FormsModule, Button, Select, TableModule, Tag],
  templateUrl: './estados.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstadosSunatPage {
  private readonly sunat = inject(SunatService);
  private readonly notificador = inject(Notificador);

  protected estado: string | null = null;
  protected readonly estados = [
    { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
    { valor: 'ACEPTADO', etiqueta: 'Aceptado' },
    { valor: 'OBSERVADO', etiqueta: 'Observado' },
    { valor: 'RECHAZADO', etiqueta: 'Rechazado' },
  ];

  protected readonly listado = new ListadoPaginado<DocumentoSunat>(
    (consulta) => this.sunat.buscar({ ...consulta, estado: this.estado ?? undefined }),
    (e) => this.notificador.error(e, 'No se pudieron cargar los documentos'),
  );

  protected filtrar(): void {
    this.listado.reiniciar();
  }

  protected reenviar(doc: DocumentoSunat): void {
    this.sunat.reenviar(doc.tipo, doc.serie, doc.correlativo).subscribe({
      next: (actualizado) => {
        this.notificador.exito(`${doc.serie}-${doc.correlativo}: ${actualizado.estado}`);
        this.listado.recargar();
      },
      error: (e: unknown) => this.notificador.error(e, 'No se pudo reenviar'),
    });
  }

  protected severidad(estado: string): 'success' | 'warn' | 'danger' | 'secondary' {
    return estado === 'ACEPTADO' || estado === 'OBSERVADO' ? 'success' : estado === 'RECHAZADO' ? 'danger' : 'secondary';
  }
}
