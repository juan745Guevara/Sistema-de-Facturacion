import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';

import { Notificador } from '../../../core/api/notificador.service';
import { CatalogoService, Producto } from '../../catalogo/catalogo.service';
import { Proveedor, ProveedoresService } from '../proveedores/proveedores.service';
import { ComprasService } from '../compras.service';

@Component({
  selector: 'app-nueva-compra',
  imports: [FormsModule, ReactiveFormsModule, Button, InputNumber, Select, TableModule],
  template: `
    <h1 class="text-2xl font-semibold mt-0 mb-3">Nueva compra</h1>
    <form [formGroup]="formulario" (ngSubmit)="guardar()" class="p-fluid">
      <div class="grid formgrid">
        <div class="field col-12 md:col-3">
          <label>Tipo</label>
          <p-select formControlName="tipo" [options]="tipos" optionLabel="etiqueta" optionValue="valor" />
        </div>
        <div class="field col-12 md:col-3">
          <label>Serie</label>
          <input class="p-inputtext w-full" formControlName="serie" />
        </div>
        <div class="field col-12 md:col-3">
          <label>Número</label>
          <input class="p-inputtext w-full" formControlName="correlativo" />
        </div>
        <div class="field col-12 md:col-3">
          <label>Proveedor</label>
          <p-select formControlName="proveedorId" [options]="proveedores()" optionLabel="nombre" optionValue="id" appendTo="body" />
        </div>
      </div>
      <p-table [value]="items()" class="mb-3">
        <ng-template #header><tr><th>Producto</th><th>Cantidad</th><th></th></tr></ng-template>
        <ng-template #body let-item let-i="rowIndex">
          <tr>
            <td>{{ item.producto.descripcion }}</td>
            <td><p-inputnumber [ngModel]="item.cantidad" [ngModelOptions]="{standalone:true}" (ngModelChange)="item.cantidad=$event" [min]="0.001" /></td>
            <td><p-button icon="pi pi-times" [text]="true" (onClick)="items.update(a => a.filter((_,j)=>j!==i))" /></td>
          </tr>
        </ng-template>
      </p-table>
      <p-select [options]="productos()" optionLabel="descripcion" placeholder="Agregar producto" (ngModelChange)="agregar($event)" [ngModel]="null" [ngModelOptions]="{standalone:true}" appendTo="body" />
      <div class="flex justify-content-end mt-3">
        <p-button type="submit" label="Registrar compra" [loading]="guardando()" [disabled]="items().length===0" />
      </div>
    </form>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NuevaCompraPage implements OnInit {
  private readonly compras = inject(ComprasService);
  private readonly proveedoresApi = inject(ProveedoresService);
  private readonly catalogo = inject(CatalogoService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly tipos = [
    { valor: 'FACTURA', etiqueta: 'Factura' },
    { valor: 'BOLETA', etiqueta: 'Boleta' },
    { valor: 'NOTA_VENTA', etiqueta: 'Nota de venta' },
  ];
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly items = signal<{ producto: Producto; cantidad: number }[]>([]);
  protected readonly guardando = signal(false);
  protected readonly formulario = this.fb.group({
    tipo: ['FACTURA', Validators.required],
    serie: ['', Validators.required],
    correlativo: ['', Validators.required],
    proveedorId: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.proveedoresApi.buscar({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.proveedores.set(p.contenido));
    this.catalogo.buscarProductos({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.productos.set(p.contenido));
  }

  protected agregar(producto: Producto | null): void {
    if (producto && !this.items().some((i) => i.producto.id === producto.id)) {
      this.items.update((a) => [...a, { producto, cantidad: 1 }]);
    }
  }

  protected guardar(): void {
    if (this.formulario.invalid || this.items().length === 0) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    this.guardando.set(true);
    this.compras
      .registrar({
        tipo: v.tipo,
        serie: v.serie,
        correlativo: v.correlativo,
        proveedorId: v.proveedorId,
        items: this.items().map((i) => ({
            productoId: i.producto.id,
            cantidad: i.cantidad,
            precioUnitario: i.producto.precioCompra ?? i.producto.precioVenta,
          })),
      })
      .subscribe({
        next: (c) => {
          this.guardando.set(false);
          this.items.set([]);
          this.notificador.exito(`Compra ${c.serie}-${c.correlativo}`);
        },
        error: (e: unknown) => {
          this.guardando.set(false);
          this.notificador.error(e, 'No se pudo registrar la compra');
        },
      });
  }
}
