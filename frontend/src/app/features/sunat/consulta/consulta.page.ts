import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Tag } from 'primeng/tag';

import { Notificador } from '../../../core/api/notificador.service';
import { DocumentoSunat, SunatService } from '../sunat.service';

@Component({
  selector: 'app-consulta-sunat',
  imports: [ReactiveFormsModule, Button, Card, InputNumber, InputText, Select, Tag],
  templateUrl: './consulta.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsultaSunatPage {
  private readonly sunat = inject(SunatService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly tipos = [
    { valor: 'FACTURA', etiqueta: 'Factura' },
    { valor: 'BOLETA', etiqueta: 'Boleta' },
    { valor: 'NOTA_CREDITO', etiqueta: 'Nota de crédito' },
    { valor: 'NOTA_DEBITO', etiqueta: 'Nota de débito' },
  ];
  protected readonly documento = signal<DocumentoSunat | null>(null);
  protected readonly buscando = signal(false);

  protected readonly formulario = this.fb.group({
    tipo: ['FACTURA', Validators.required],
    serie: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]],
    correlativo: this.fb.control<number | null>(null, Validators.required),
  });

  protected consultar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { tipo, serie, correlativo } = this.formulario.getRawValue();
    this.buscando.set(true);
    this.sunat.obtener(tipo, serie.toUpperCase(), correlativo as number).subscribe({
      next: (doc) => {
        this.buscando.set(false);
        this.documento.set(doc);
      },
      error: (e: unknown) => {
        this.buscando.set(false);
        this.documento.set(null);
        this.notificador.error(e, 'Documento no encontrado');
      },
    });
  }
}
