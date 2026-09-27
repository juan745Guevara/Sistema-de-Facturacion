import { Injectable, inject } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';

import { mensajeDeError } from './api-error';

@Injectable({ providedIn: 'root' })
export class Notificador {
  private readonly mensajes = inject(MessageService);
  private readonly confirmaciones = inject(ConfirmationService);

  exito(detalle: string): void {
    this.mensajes.add({ severity: 'success', summary: 'Listo', detail: detalle, life: 3000 });
  }

  aviso(detalle: string): void {
    this.mensajes.add({ severity: 'warn', summary: 'Atención', detail: detalle, life: 6000 });
  }

  error(error: unknown, porDefecto?: string): void {
    this.mensajes.add({ severity: 'error', summary: 'Error', detail: mensajeDeError(error, porDefecto), life: 6000 });
  }

  confirmarEliminacion(descripcion: string, alAceptar: () => void): void {
    this.confirmaciones.confirm({
      header: 'Confirmar eliminación',
      message: `¿Eliminar ${descripcion}? Esta acción no se puede deshacer.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', text: true },
      accept: alAceptar,
    });
  }
}
