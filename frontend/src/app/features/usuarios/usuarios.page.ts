import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Checkbox } from 'primeng/checkbox';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Observable } from 'rxjs';

import { Notificador } from '../../core/api/notificador.service';
import { Rol } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { sinVacios } from '../../shared/utils/formularios';
import { CambiosUsuario, LONGITUD_MINIMA_PASSWORD, NuevoUsuario, Usuario, UsuariosService } from './usuarios.service';

type Dialogo = 'nuevo' | 'editar' | 'password' | null;

const ROLES: { valor: Rol; etiqueta: string }[] = [
  { valor: 'ADMINISTRADOR', etiqueta: 'Administrador' },
  { valor: 'ESPECIAL', etiqueta: 'Especial' },
  { valor: 'VENDEDOR', etiqueta: 'Vendedor' },
];

function clavesIguales(grupo: AbstractControl): ValidationErrors | null {
  const password = grupo.get('password')?.value as string;
  const confirmacion = grupo.get('confirmacion')?.value as string;
  return confirmacion && password !== confirmacion ? { clavesDistintas: true } : null;
}

@Component({
  selector: 'app-usuarios',
  imports: [DatePipe, ReactiveFormsModule, Button, Checkbox, Dialog, InputText, Password, Select, TableModule, Tag],
  templateUrl: './usuarios.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsuariosPage implements OnInit {
  private readonly usuarios = inject(UsuariosService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly idPropio = inject(AuthService).usuario()?.id;

  protected readonly roles = ROLES;
  protected readonly longitudMinima = LONGITUD_MINIMA_PASSWORD;
  protected readonly lista = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly dialogo = signal<Dialogo>(null);
  protected readonly seleccionado = signal<Usuario | null>(null);
  protected readonly esPropio = computed(() => this.seleccionado()?.id === this.idPropio);

  private readonly clave = () => ({
    password: ['', [Validators.required, Validators.minLength(LONGITUD_MINIMA_PASSWORD), Validators.maxLength(100)]],
    confirmacion: ['', Validators.required],
  });

  protected readonly formularioNuevo = this.fb.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      username: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9._-]{3,50}$/)]],
      email: ['', [Validators.email, Validators.maxLength(120)]],
      rol: this.fb.control<Rol>('VENDEDOR', Validators.required),
      ...this.clave(),
    },
    { validators: clavesIguales },
  );

  protected readonly formularioEdicion = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.email, Validators.maxLength(120)]],
    rol: this.fb.control<Rol>('VENDEDOR', Validators.required),
    activo: [true],
  });

  protected readonly formularioPassword = this.fb.group(this.clave(), { validators: clavesIguales });

  ngOnInit(): void {
    this.cargar();
  }

  protected etiquetaRol(rol: Rol): string {
    return ROLES.find((r) => r.valor === rol)?.etiqueta ?? rol;
  }

  protected invalido(formulario: AbstractControl, campo: string): boolean {
    const control = formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  protected cerrar(): void {
    this.dialogo.set(null);
  }

  protected nuevo(): void {
    this.formularioNuevo.reset();
    this.dialogo.set('nuevo');
  }

  protected editar(usuario: Usuario): void {
    this.seleccionado.set(usuario);
    this.formularioEdicion.reset({
      nombre: usuario.nombre,
      email: usuario.email ?? '',
      rol: usuario.rol,
      activo: usuario.activo,
    });
    this.dialogo.set('editar');
  }

  protected cambiarPassword(usuario: Usuario): void {
    this.seleccionado.set(usuario);
    this.formularioPassword.reset();
    this.dialogo.set('password');
  }

  protected crear(): void {
    if (this.formularioNuevo.invalid) {
      this.formularioNuevo.markAllAsTouched();
      return;
    }
    const { confirmacion: _confirmacion, password, ...datos } = this.formularioNuevo.getRawValue();
    const usuario = { ...sinVacios(datos), password } as NuevoUsuario;
    this.ejecutar(this.usuarios.crear(usuario), `Usuario ${usuario.username} creado`);
  }

  protected actualizar(): void {
    const usuario = this.seleccionado();
    if (!usuario || this.formularioEdicion.invalid) {
      this.formularioEdicion.markAllAsTouched();
      return;
    }
    const cambios = sinVacios(this.formularioEdicion.getRawValue()) as CambiosUsuario;
    this.ejecutar(this.usuarios.actualizar(usuario.id, cambios), `Usuario ${usuario.username} actualizado`);
  }

  protected guardarPassword(): void {
    const usuario = this.seleccionado();
    if (!usuario || this.formularioPassword.invalid) {
      this.formularioPassword.markAllAsTouched();
      return;
    }
    this.ejecutar(
      this.usuarios.cambiarPassword(usuario.id, this.formularioPassword.controls.password.value),
      `Contraseña de ${usuario.username} actualizada`,
    );
  }

  private ejecutar(solicitud: Observable<unknown>, exito: string): void {
    this.guardando.set(true);
    solicitud.subscribe({
      next: () => {
        this.guardando.set(false);
        this.dialogo.set(null);
        this.notificador.exito(exito);
        this.cargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar el usuario');
      },
    });
  }

  private cargar(): void {
    this.usuarios.listar().subscribe({
      next: (usuarios) => {
        this.lista.set(usuarios);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.notificador.error(e, 'No se pudieron cargar los usuarios');
      },
    });
  }
}
