import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';

import { Notificador } from '../../../core/api/notificador.service';
import { AuthService } from '../../../core/auth/auth.service';
import { CatalogoService, Categoria } from '../catalogo.service';

@Component({
  selector: 'app-categorias',
  imports: [ReactiveFormsModule, Button, Dialog, IconField, InputIcon, InputText, TableModule],
  templateUrl: './categorias.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriasPage implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly notificador = inject(Notificador);

  protected readonly puedeEditar = inject(AuthService).tieneRol('ADMINISTRADOR', 'ESPECIAL');
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly filtro = signal('');
  protected readonly cargando = signal(true);
  protected readonly visibles = computed(() => {
    const texto = this.filtro().trim().toUpperCase();
    return texto ? this.categorias().filter((c) => c.nombre.includes(texto)) : this.categorias();
  });

  protected readonly dialogoAbierto = signal(false);
  protected readonly editando = signal<Categoria | null>(null);
  protected readonly guardando = signal(false);
  protected readonly formulario = inject(NonNullableFormBuilder).group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });
  protected readonly nombre = this.formulario.controls.nombre;

  ngOnInit(): void {
    this.cargar();
  }

  protected nueva(): void {
    this.abrir(null);
  }

  protected abrir(categoria: Categoria | null): void {
    this.editando.set(categoria);
    this.nombre.reset(categoria?.nombre ?? '');
    this.dialogoAbierto.set(true);
  }

  protected guardar(): void {
    if (this.nombre.invalid || this.nombre.value.trim() === '') {
      this.nombre.markAsTouched();
      return;
    }
    const nombre = this.nombre.value.trim();
    const actual = this.editando();
    this.guardando.set(true);
    const solicitud = actual
      ? this.catalogo.renombrarCategoria(actual.id, nombre)
      : this.catalogo.crearCategoria(nombre);
    solicitud.subscribe({
      next: (guardada) => {
        this.guardando.set(false);
        this.dialogoAbierto.set(false);
        this.notificador.exito(`Categoría ${guardada.nombre} guardada`);
        this.cargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar la categoría');
      },
    });
  }

  protected eliminar(categoria: Categoria): void {
    this.notificador.confirmarEliminacion(`la categoría ${categoria.nombre}`, () =>
      this.catalogo.eliminarCategoria(categoria.id).subscribe({
        next: () => {
          this.notificador.exito('Categoría eliminada');
          this.cargar();
        },
        error: (e: unknown) => this.notificador.error(e, 'No se pudo eliminar la categoría'),
      }),
    );
  }

  private cargar(): void {
    this.cargando.set(true);
    this.catalogo.listarCategorias().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.notificador.error(e, 'No se pudieron cargar las categorías');
      },
    });
  }
}
