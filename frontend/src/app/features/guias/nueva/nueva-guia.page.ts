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
import { GuiasService, Ubigeo } from '../guias.service';

@Component({
  selector: 'app-nueva-guia',
  imports: [FormsModule, ReactiveFormsModule, Button, InputNumber, Select, TableModule],
  template: `
    <h1 class="text-2xl font-semibold mt-0 mb-3">Nueva guía de remisión</h1>
    <form [formGroup]="formulario" (ngSubmit)="guardar()" class="p-fluid">
      <div class="grid formgrid">
        <div class="field col-12 md:col-3">
          <label>Serie</label>
          <p-select formControlName="serie" [options]="series()" optionLabel="serie" optionValue="serie" />
        </div>
        <div class="field col-12 md:col-5">
          <label>Destinatario</label>
          <p-select formControlName="clienteId" [options]="clientes()" optionLabel="nombre" optionValue="id" appendTo="body" />
        </div>
        <div class="field col-12 md:col-2">
          <label>Motivo</label>
          <p-select formControlName="motivoTraslado" [options]="motivos" optionLabel="etiqueta" optionValue="valor" />
        </div>
        <div class="field col-12 md:col-2">
          <label>Modalidad</label>
          <p-select formControlName="modalidad" [options]="modalidades" optionLabel="etiqueta" optionValue="valor" />
        </div>
        <div class="field col-12 md:col-3">
          <label>Fecha de traslado</label>
          <input class="p-inputtext w-full" type="date" formControlName="fechaTraslado" />
        </div>
        <div class="field col-12 md:col-3">
          <label>Peso (kg)</label>
          <p-inputnumber formControlName="pesoTotal" [min]="0.001" [minFractionDigits]="3" />
        </div>
        <div class="field col-12 md:col-2">
          <label>Bultos</label>
          <p-inputnumber formControlName="bultos" [min]="1" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Partida</label>
          <p-select formControlName="ubigeoPartida" [options]="ubigeos()" optionLabel="etiqueta" optionValue="codigo" appendTo="body" [filter]="true" (onFilter)="buscarUbigeo($event.filter)" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Dirección partida</label>
          <input class="p-inputtext w-full" formControlName="direccionPartida" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Llegada</label>
          <p-select formControlName="ubigeoLlegada" [options]="ubigeos()" optionLabel="etiqueta" optionValue="codigo" appendTo="body" [filter]="true" (onFilter)="buscarUbigeo($event.filter)" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Dirección llegada</label>
          <input class="p-inputtext w-full" formControlName="direccionLlegada" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Transportista</label>
          <input class="p-inputtext w-full" formControlName="transportistaNombre" />
        </div>
        <div class="field col-12 md:col-4">
          <label>Placa</label>
          <input class="p-inputtext w-full" formControlName="placa" />
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
        <p-button type="submit" label="Emitir guía" [loading]="guardando()" [disabled]="items().length===0" />
      </div>
    </form>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NuevaGuiaPage implements OnInit {
  private readonly guias = inject(GuiasService);
  private readonly clientesApi = inject(ClientesService);
  private readonly catalogo = inject(CatalogoService);
  private readonly ventas = inject(VentasService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly motivos = [
    { valor: '01', etiqueta: 'Venta' },
    { valor: '02', etiqueta: 'Compra' },
    { valor: '04', etiqueta: 'Traslado entre establecimientos' },
    { valor: '13', etiqueta: 'Otros' },
  ];
  protected readonly modalidades = [
    { valor: '01', etiqueta: 'Transporte público' },
    { valor: '02', etiqueta: 'Transporte privado' },
  ];
  protected readonly series = signal<Serie[]>([]);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly ubigeos = signal<(Ubigeo & { etiqueta: string })[]>([]);
  protected readonly items = signal<{ producto: Producto; cantidad: number }[]>([]);
  protected readonly guardando = signal(false);
  protected readonly formulario = this.fb.group({
    serie: ['T001', Validators.required],
    clienteId: this.fb.control<number | null>(null, Validators.required),
    motivoTraslado: ['01', Validators.required],
    modalidad: ['02', Validators.required],
    fechaTraslado: [new Date().toISOString().slice(0, 10), Validators.required],
    pesoTotal: [1, Validators.required],
    bultos: [1, Validators.required],
    ubigeoPartida: ['150101', Validators.required],
    direccionPartida: ['', Validators.required],
    ubigeoLlegada: ['150101', Validators.required],
    direccionLlegada: ['', Validators.required],
    transportistaNombre: [''],
    placa: [''],
  });

  ngOnInit(): void {
    this.ventas.series('GUIA_REMISION').subscribe((s) => {
      this.series.set(s.filter((x) => x.activa));
      if (s[0]) {
        this.formulario.controls.serie.setValue(s[0].serie);
      }
    });
    this.clientesApi.buscar({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.clientes.set(p.contenido));
    this.catalogo.buscarProductos({ q: '', pagina: 0, tamanio: 20 }).subscribe((p) => this.productos.set(p.contenido));
    this.buscarUbigeo('lima');
  }

  protected buscarUbigeo(q: string): void {
    this.guias.ubigeos(q ?? '').subscribe((lista) =>
      this.ubigeos.set(lista.map((u) => ({ ...u, etiqueta: `${u.codigo} ${u.distrito}` }))),
    );
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
    this.guias
      .emitir({
        ...v,
        items: this.items().map((i) => ({ productoId: i.producto.id, cantidad: i.cantidad })),
      })
      .subscribe({
        next: (g) => {
          this.guardando.set(false);
          this.items.set([]);
          this.notificador.exito(`Guía ${g.serie}-${g.correlativo} (${g.estadoSunat})`);
        },
        error: (e: unknown) => {
          this.guardando.set(false);
          this.notificador.error(e, 'No se pudo emitir la guía');
        },
      });
  }
}
