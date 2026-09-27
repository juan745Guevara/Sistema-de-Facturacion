import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { SelectButton } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { Notificador } from '../../../core/api/notificador.service';
import { AuthService } from '../../../core/auth/auth.service';
import { CatalogoService, UnidadMedida } from '../catalogo.service';

type Vista = 'todas' | 'activas' | 'inactivas';

@Component({
  selector: 'app-unidades',
  imports: [FormsModule, IconField, InputIcon, InputText, SelectButton, TableModule, ToggleSwitch],
  templateUrl: './unidades.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnidadesPage implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly notificador = inject(Notificador);

  protected readonly puedeEditar = inject(AuthService).tieneRol('ADMINISTRADOR');
  protected readonly unidades = signal<UnidadMedida[]>([]);
  protected readonly cargando = signal(true);
  protected readonly actualizando = signal<string | null>(null);
  protected readonly filtro = signal('');
  protected readonly vista = signal<Vista>('todas');
  protected readonly vistas: { label: string; value: Vista }[] = [
    { label: 'Todas', value: 'todas' },
    { label: 'Activas', value: 'activas' },
    { label: 'Inactivas', value: 'inactivas' },
  ];

  protected readonly visibles = computed(() => {
    const texto = this.filtro().trim().toUpperCase();
    const vista = this.vista();
    return this.unidades().filter(
      (u) =>
        (vista === 'todas' || u.activa === (vista === 'activas')) &&
        (!texto || u.codigo.includes(texto) || u.descripcion.includes(texto)),
    );
  });

  ngOnInit(): void {
    this.catalogo.listarUnidades().subscribe({
      next: (unidades) => {
        this.unidades.set(unidades);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.notificador.error(e, 'No se pudieron cargar las unidades');
      },
    });
  }

  protected cambiar(unidad: UnidadMedida, activa: boolean): void {
    this.actualizando.set(unidad.codigo);
    this.catalogo.cambiarActivacion(unidad.codigo, activa).subscribe({
      next: (actualizada) => {
        this.actualizando.set(null);
        this.unidades.update((lista) => lista.map((u) => (u.codigo === actualizada.codigo ? actualizada : u)));
      },
      error: (e: unknown) => {
        this.actualizando.set(null);
        this.unidades.update((lista) => lista.map((u) => (u.codigo === unidad.codigo ? { ...u } : u)));
        this.notificador.error(e, 'No se pudo actualizar la unidad');
      },
    });
  }
}
