import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';

import { Notificador } from '../../../core/api/notificador.service';
import { Cliente, ClientesService } from '../../clientes/clientes.service';
import { CatalogoService, Producto } from '../../catalogo/catalogo.service';
import { Serie, VentasService } from '../../ventas/ventas.service';
import { CotizacionesService } from '../cotizaciones.service';

@Component({
  selector: 'app-nueva-cotizacion',
  imports: [FormsModule, ReactiveFormsModule, Button, InputNumber, Select, TableModule],
  template: `
    <h1 class="text-2xl font-semibold mt-0 mb-3">Nueva cotización</h1>
    <form [formGroup]="formulario" (ngSubmit)="guardar()" class="p-fluid">
      <div class="grid formgrid">
        <div class="field col-12 md:col-4">
          <label>Serie</label>
          <p-select formControlName="serie" [options]="series()" optionLabel="serie" optionValue="serie" />
        </div>
        <div class="field col-12 md:col-8">
          <label>Cliente</label>
          <p-select formControlName="clienteId" [options]="clientes()" optionLabel="nombre" optionValue="id" appendTo="body" />
        </div>
      </div>
      <p-table [value]="items()" class="mb-3">
        <ng-template #header><tr><th>Producto</th><th>Cantidad</th><th>Precio</th><th></th></tr></ng-template>
        <ng-template #body let-item let-i="rowIndex">
          <tr>
            <td>{{ item.producto.descripcion }}</td>
            <td><p-inputnumber [ngModel]="item.cantidad" [ngModelOptions]="{standalone:true}" (ngModelChange)="item.cantidad=$event" [min]="0.001" /></td>
            <td><p-inputnumber [ngModel]="item.precioUnitario" [ngModelOptions]="{standalone:true}" (ngModelChange)="item.precioUnitario=$event" mode="currency" currency="PEN" /></td>
            <td><p-button icon="pi pi-times" [text]="true" (onClick)="items.update(a => a.filter((_,j)=>j!==i))" /></td>
          </tr>
        </ng-template>
      </p-table>
      <p-select [options]="productos()" optionLabel="descripcion" placeholder="Agregar producto" (ngModelChange)="agregar($event)" [ngModel]="null" [ngModelOptions]="{standalone:true}" appendTo="body" />
      <div class="flex justify-content-end mt-3">
        <p-button type="submit" label="Guardar cotización" [loading]="guardando()" [disabled]="items().length===0" />
      </div>
    </form>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NuevaCotizacionPage implements OnInit {
  private readonly cotizaciones = inject(CotizacionesService);
  private readonly clientesApi = inject(ClientesService);
  private readonly catalogo = inject(CatalogoService);
  private readonly ventas = inject(VentasService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly series = signal<Serie[]>([]);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly items = signal<{ producto: Producto; cantidad: number; precioUnitario: number }[]>([]);
  protected readonly guardando = signal(false);
  protected readonly formulario = this.fb.group({
    serie: ['C001', Validators.required],
    clienteId: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.ventas.series('COTIZACION').subscribe((s) => {
      this.series.set(s.filter((x) => x.activa));
      if (s[0]) {
        this.formulario.controls.serie.setValue(s[0].serie);
      }
    });
    this.clientesApi.buscar({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.clientes.set(p.contenido));
    this.catalogo.buscarProductos({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.productos.set(p.contenido));
  }

  protected agregar(producto: Producto | null): void {
    if (producto && !this.items().some((i) => i.producto.id === producto.id)) {
      this.items.update((a) => [...a, { producto, cantidad: 1, precioUnitario: producto.precioVenta }]);
    }
  }

  protected guardar(): void {
    if (this.formulario.invalid || this.items().length === 0) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    this.guardando.set(true);
    this.cotizaciones
      .emitir({
        serie: v.serie,
        clienteId: v.clienteId,
        items: this.items().map((i) => ({
          productoId: i.producto.id,
          cantidad: i.cantidad,
          precioUnitario: i.precioUnitario,
        })),
      })
      .subscribe({
        next: (c) => {
          this.guardando.set(false);
          this.items.set([]);
          this.notificador.exito(`Cotización ${c.serie}-${c.correlativo}`);
        },
        error: (e: unknown) => {
          this.guardando.set(false);
          this.notificador.error(e, 'No se pudo guardar la cotización');
        },
      });
  }
}
