import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Textarea } from 'primeng/textarea';
import { map, startWith } from 'rxjs';

import { CatalogosSunatService } from '../../../core/api/catalogos-sunat.service';
import { EmpresaService, IGV_POR_DEFECTO } from '../../../core/api/empresa.service';
import { Notificador } from '../../../core/api/notificador.service';
import { AuthService } from '../../../core/auth/auth.service';
import { MontoPipe } from '../../../shared/pipes/monto.pipe';
import { sinVacios } from '../../../shared/utils/formularios';
import { ListadoPaginado } from '../../../shared/utils/listado-paginado';
import { CatalogoService, Categoria, DatosProducto, Producto, UnidadMedida } from '../catalogo.service';
import { desglosarPrecio, precioCompraSugerido } from './precios';

@Component({
  selector: 'app-productos',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    Button,
    Dialog,
    IconField,
    InputIcon,
    InputNumber,
    InputText,
    MontoPipe,
    Select,
    TableModule,
    Textarea,
  ],
  templateUrl: './productos.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductosPage implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly empresas = inject(EmpresaService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly puedeEditar = inject(AuthService).tieneRol('ADMINISTRADOR', 'ESPECIAL');
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly unidades = signal<UnidadMedida[]>([]);
  protected readonly porcentajeIgv = signal(IGV_POR_DEFECTO);
  protected readonly categoriaFiltro = signal<number | null>(null);
  protected readonly afectaciones = toSignal(
    inject(CatalogosSunatService).tiposAfectacionIgv$.pipe(
      map((tipos) => tipos.map((t) => ({ valor: t.valor, etiqueta: `${t.codigo} - ${t.descripcion}` }))),
    ),
    { initialValue: [] },
  );

  protected readonly listado = new ListadoPaginado<Producto>(
    (consulta) => this.catalogo.buscarProductos({ ...consulta, categoriaId: this.categoriaFiltro() }),
    (e) => this.notificador.error(e, 'No se pudieron cargar los productos'),
  );

  private readonly nombresCategoria = computed(() => new Map(this.categorias().map((c) => [c.id, c.nombre])));

  protected readonly dialogoAbierto = signal(false);
  protected readonly editando = signal<Producto | null>(null);
  protected readonly guardando = signal(false);

  protected readonly formulario = this.fb.group({
    codigo: ['', [Validators.required, Validators.maxLength(30)]],
    descripcion: ['', [Validators.required, Validators.maxLength(500)]],
    categoriaId: this.fb.control<number | null>(null, Validators.required),
    unidadMedida: ['NIU', Validators.required],
    tipoAfectacionIgv: ['GRAVADO_ONEROSO', Validators.required],
    precioVenta: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    precioCompra: this.fb.control<number | null>(null, Validators.min(0)),
    stock: this.fb.control<number | null>(0, Validators.min(0)),
  });

  private readonly valores = toSignal(
    this.formulario.valueChanges.pipe(
      startWith(null),
      map(() => this.formulario.getRawValue()),
    ),
    { requireSync: true },
  );

  protected readonly desglose = computed(() => {
    const { precioVenta, tipoAfectacionIgv } = this.valores();
    return desglosarPrecio(precioVenta, tipoAfectacionIgv, this.porcentajeIgv());
  });

  /** Unidades activas más la actual del producto, que puede haberse desactivado después. */
  protected readonly opcionesUnidad = computed(() => {
    const actual = this.editando()?.unidadMedida;
    const unidades = this.unidades().filter((u) => u.activa || u.codigo === actual);
    return unidades.map((u) => ({ valor: u.codigo, etiqueta: `${u.codigo} - ${u.descripcion}` }));
  });

  constructor() {
    this.formulario.controls.precioVenta.valueChanges.pipe(takeUntilDestroyed()).subscribe((precio) => {
      const compra = this.formulario.controls.precioCompra;
      if (!this.editando() && !compra.dirty) {
        compra.setValue(precioCompraSugerido(precio));
      }
    });
  }

  ngOnInit(): void {
    this.catalogo.listarCategorias().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar las categorías'),
    });
    this.catalogo.listarUnidades().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: (e: unknown) => this.notificador.error(e, 'No se pudieron cargar las unidades'),
    });
    this.empresas.obtener().subscribe({
      next: (empresa) => this.porcentajeIgv.set(empresa?.porcentajeIgv ?? IGV_POR_DEFECTO),
      error: () => this.porcentajeIgv.set(IGV_POR_DEFECTO),
    });
  }

  protected nombreCategoria(id: number): string {
    return this.nombresCategoria().get(id) ?? '—';
  }

  protected filtrarPorCategoria(id: number | null): void {
    this.categoriaFiltro.set(id);
    this.listado.reiniciar();
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.dialogoAbierto.set(true);
  }

  protected editar(producto: Producto): void {
    this.editando.set(producto);
    this.formulario.reset({
      codigo: producto.codigo,
      descripcion: producto.descripcion,
      categoriaId: producto.categoriaId,
      unidadMedida: producto.unidadMedida,
      tipoAfectacionIgv: producto.tipoAfectacionIgv,
      precioVenta: producto.precioVenta,
      precioCompra: producto.precioCompra,
      stock: producto.stock,
    });
    this.dialogoAbierto.set(true);
  }

  protected invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const producto = sinVacios(this.formulario.getRawValue()) as DatosProducto;
    const actual = this.editando();
    this.guardando.set(true);
    const solicitud = actual
      ? this.catalogo.actualizarProducto(actual.id, producto)
      : this.catalogo.crearProducto(producto);
    solicitud.subscribe({
      next: (guardado) => {
        this.guardando.set(false);
        this.dialogoAbierto.set(false);
        this.notificador.exito(`Producto ${guardado.codigo} guardado`);
        this.listado.recargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar el producto');
      },
    });
  }

  protected eliminar(producto: Producto): void {
    this.notificador.confirmarEliminacion(`el producto ${producto.codigo}`, () =>
      this.catalogo.eliminarProducto(producto.id).subscribe({
        next: () => {
          this.notificador.exito('Producto eliminado');
          this.listado.recargar();
        },
        error: (e: unknown) => this.notificador.error(e, 'No se pudo eliminar el producto'),
      }),
    );
  }
}
