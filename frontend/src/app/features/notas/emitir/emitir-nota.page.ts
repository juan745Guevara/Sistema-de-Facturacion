import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';

import { CatalogosSunatService, OpcionCatalogo } from '../../../core/api/catalogos-sunat.service';
import { Notificador } from '../../../core/api/notificador.service';
import { MontoPipe } from '../../../shared/pipes/monto.pipe';
import { Calculo, Serie, Venta, VentasService } from '../../ventas/ventas.service';
import { NotasService, TipoNota, TipoReferencia } from '../notas.service';

const TITULOS: Record<TipoNota, string> = {
  NOTA_CREDITO: 'Emitir nota de crédito',
  NOTA_DEBITO: 'Emitir nota de débito',
};

@Component({
  selector: 'app-emitir-nota',
  imports: [FormsModule, ReactiveFormsModule, Button, InputNumber, Select, TableModule, MontoPipe],
  templateUrl: './emitir-nota.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmitirNotaPage implements OnInit {
  private readonly notas = inject(NotasService);
  private readonly ventas = inject(VentasService);
  private readonly catalogos = inject(CatalogosSunatService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly tipo = input.required<TipoNota>();
  protected readonly titulo = computed(() => TITULOS[this.tipo()]);

  protected readonly series = signal<Serie[]>([]);
  protected readonly motivos = signal<OpcionCatalogo[]>([]);
  protected readonly origen = signal<Venta | null>(null);
  protected readonly calculo = signal<Calculo | null>(null);
  protected readonly emitiendo = signal(false);
  protected readonly tiposReferencia: { valor: TipoReferencia; etiqueta: string }[] = [
    { valor: 'FACTURA', etiqueta: 'Factura' },
    { valor: 'BOLETA', etiqueta: 'Boleta' },
  ];

  protected readonly formulario = this.fb.group({
    serie: ['', Validators.required],
    tipoReferencia: this.fb.control<TipoReferencia>('FACTURA', Validators.required),
    serieReferencia: ['', Validators.required],
    correlativoReferencia: this.fb.control<number | null>(null, Validators.required),
    codigoMotivo: ['', Validators.required],
  });

  ngOnInit(): void {
    this.ventas.series(this.tipo()).subscribe({
      next: (series) => {
        const activas = series.filter((s) => s.activa);
        this.series.set(activas);
        if (activas[0]) {
          this.formulario.controls.serie.setValue(activas[0].serie);
        }
      },
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar las series'),
    });
    const catalogo$ =
      this.tipo() === 'NOTA_CREDITO' ? this.catalogos.motivosNotaCredito$ : this.catalogos.motivosNotaDebito$;
    catalogo$.subscribe({
      next: (motivos) => this.motivos.set(motivos),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar los motivos'),
    });
  }

  protected buscarOrigen(): void {
    const { tipoReferencia, serieReferencia, correlativoReferencia, codigoMotivo } = this.formulario.getRawValue();
    if (!serieReferencia || !correlativoReferencia) {
      return;
    }
    this.ventas.obtenerPorNumero(tipoReferencia, serieReferencia, correlativoReferencia).subscribe({
      next: (venta) => {
        this.origen.set(venta);
        if (codigoMotivo) {
          this.previsualizar(venta, codigoMotivo);
        }
      },
      error: (e: unknown) => {
        this.origen.set(null);
        this.calculo.set(null);
        this.notificador.error(e, 'No se encontró el comprobante');
      },
    });
  }

  protected alCambiarMotivo(): void {
    const origen = this.origen();
    const motivo = this.formulario.controls.codigoMotivo.value;
    if (origen && motivo) {
      this.previsualizar(origen, motivo);
    }
  }

  protected emitir(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    this.emitiendo.set(true);
    this.notas
      .emitir({
        tipo: this.tipo(),
        serie: v.serie,
        tipoReferencia: v.tipoReferencia,
        serieReferencia: v.serieReferencia,
        correlativoReferencia: v.correlativoReferencia as number,
        codigoMotivo: this.codigoMotivo(v.codigoMotivo),
      })
      .subscribe({
        next: (nota) => {
          this.emitiendo.set(false);
          this.origen.set(null);
          this.calculo.set(null);
          this.notificador.exito(`${this.titulo()} ${nota.serie}-${nota.correlativo}`);
        },
        error: (e: unknown) => {
          this.emitiendo.set(false);
          this.notificador.error(e, 'No se pudo emitir la nota');
        },
      });
  }

  private previsualizar(venta: Venta, motivo: string): void {
    this.notas
      .previsualizar({
        tipoReferencia: venta.tipo as TipoReferencia,
        serieReferencia: venta.serie,
        correlativoReferencia: venta.correlativo,
        codigoMotivo: this.codigoMotivo(motivo),
      })
      .subscribe({
        next: (c) => this.calculo.set(c),
        error: (e: unknown) => this.notificador.error(e, 'No se pudo calcular'),
      });
  }

  private codigoMotivo(valor: string): string {
    return this.motivos().find((m) => m.valor === valor)?.codigo ?? valor;
  }
}
