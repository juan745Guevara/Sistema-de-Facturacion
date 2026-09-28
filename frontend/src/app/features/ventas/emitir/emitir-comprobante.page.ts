import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { debounceTime, filter, switchMap } from 'rxjs';

import { Notificador } from '../../../core/api/notificador.service';
import { Cliente, ClientesService } from '../../clientes/clientes.service';
import { CatalogoService, Producto } from '../../catalogo/catalogo.service';
import { MontoPipe } from '../../../shared/pipes/monto.pipe';
import { Calculo, TipoVenta, VentasService } from '../ventas.service';

const TITULOS: Record<TipoVenta, string> = {
  FACTURA: 'Emitir factura',
  BOLETA: 'Emitir boleta',
  NOTA_VENTA: 'Emitir nota de venta',
};

@Component({
  selector: 'app-emitir-comprobante',
  imports: [FormsModule, ReactiveFormsModule, Button, InputNumber, Select, TableModule, MontoPipe],
  templateUrl: './emitir-comprobante.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmitirComprobantePage implements OnInit {
  private readonly ventas = inject(VentasService);
  private readonly clientesApi = inject(ClientesService);
  private readonly catalogo = inject(CatalogoService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly tipo = input.required<TipoVenta>();
  protected readonly titulo = computed(() => TITULOS[this.tipo()]);

  protected readonly series = signal<{ serie: string }[]>([]);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly calculo = signal<Calculo | null>(null);
  protected readonly emitiendo = signal(false);

  protected readonly formulario = this.fb.group({
    serie: ['', Validators.required],
    clienteId: this.fb.control<number | null>(null, Validators.required),
  });

  protected readonly items = signal<
    { producto: Producto; cantidad: number; precioUnitario: number; descuento: number; icbper: boolean }[]
  >([]);

  constructor() {
    toObservable(this.items)
      .pipe(
        debounceTime(250),
        filter((items) => items.length > 0),
        switchMap((items) =>
          this.ventas.previsualizar(
            items.map((i) => ({
              productoId: i.producto.id,
              cantidad: i.cantidad,
              precioUnitario: i.precioUnitario,
              descuento: i.descuento || null,
              icbper: i.icbper,
            })),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe({
        next: (c) => this.calculo.set(c),
        error: (e: unknown) => this.notificador.error(e, 'No se pudo calcular'),
      });
  }

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
    this.clientesApi.buscar({ q: '', pagina: 0, tamanio: 20 }).subscribe({
      next: (p) => this.clientes.set(p.contenido),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar los clientes'),
    });
    this.catalogo.buscarProductos({ q: '', pagina: 0, tamanio: 20 }).subscribe({
      next: (p) => this.productos.set(p.contenido),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar los productos'),
    });
  }

  protected buscarClientes(texto: string): void {
    this.clientesApi.buscar({ q: texto, pagina: 0, tamanio: 20 }).subscribe((p) => this.clientes.set(p.contenido));
  }

  protected buscarProductos(texto: string): void {
    this.catalogo.buscarProductos({ q: texto, pagina: 0, tamanio: 20 }).subscribe((p) => this.productos.set(p.contenido));
  }

  protected agregar(producto: Producto | null): void {
    if (!producto || this.items().some((i) => i.producto.id === producto.id)) {
      return;
    }
    this.items.update((actual) => [
      ...actual,
      { producto, cantidad: 1, precioUnitario: producto.precioVenta, descuento: 0, icbper: false },
    ]);
  }

  protected quitar(indice: number): void {
    this.items.update((actual) => actual.filter((_, i) => i !== indice));
    if (this.items().length === 0) {
      this.calculo.set(null);
    }
  }

  protected actualizar(indice: number, campo: 'cantidad' | 'precioUnitario' | 'descuento', valor: number | null): void {
    this.items.update((actual) =>
      actual.map((item, i) => (i === indice ? { ...item, [campo]: valor ?? 0 } : item)),
    );
  }

  protected emitir(): void {
    if (this.formulario.invalid || this.items().length === 0) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.emitiendo.set(true);
    this.ventas
      .emitir({
        tipo: this.tipo(),
        serie: this.formulario.controls.serie.value,
        clienteId: this.formulario.controls.clienteId.value as number,
        moneda: 'PEN',
        formaPago: 'CONTADO',
        items: this.items().map((i) => ({
          productoId: i.producto.id,
          cantidad: i.cantidad,
          precioUnitario: i.precioUnitario,
          descuento: i.descuento || null,
          icbper: i.icbper,
        })),
      })
      .subscribe({
        next: (venta) => {
          this.emitiendo.set(false);
          this.items.set([]);
          this.calculo.set(null);
          this.notificador.exito(`${this.titulo()} ${venta.serie}-${venta.correlativo}`);
        },
        error: (e: unknown) => {
          this.emitiendo.set(false);
          this.notificador.error(e, 'No se pudo emitir el comprobante');
        },
      });
  }
}
