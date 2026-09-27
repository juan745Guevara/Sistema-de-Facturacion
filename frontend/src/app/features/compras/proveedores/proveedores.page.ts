import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';
import { map, startWith } from 'rxjs';

import { CatalogosSunatService, TipoDocumentoIdentidad } from '../../../core/api/catalogos-sunat.service';
import { Notificador } from '../../../core/api/notificador.service';
import { PadronService } from '../../../core/api/padron.service';
import { AuthService } from '../../../core/auth/auth.service';
import { abreviaturaDocumento, documentoConsultable } from '../../../shared/utils/documentos';
import { sinVacios } from '../../../shared/utils/formularios';
import { ListadoPaginado } from '../../../shared/utils/listado-paginado';
import { ErrorDocumento, MENSAJES_DOCUMENTO, documentoSegunTipo } from '../../../shared/validators/documento.validators';
import { DatosProveedor, Proveedor, ProveedoresService } from './proveedores.service';

@Component({
  selector: 'app-proveedores',
  imports: [ReactiveFormsModule, Button, Dialog, IconField, InputIcon, InputText, Select, TableModule, Tooltip],
  templateUrl: './proveedores.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProveedoresPage {
  private readonly proveedores = inject(ProveedoresService);
  private readonly padron = inject(PadronService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly puedeEditar = inject(AuthService).tieneRol('ADMINISTRADOR', 'ESPECIAL');
  protected readonly tiposDocumento = toSignal(inject(CatalogosSunatService).tiposDocumentoIdentidad$, {
    initialValue: [],
  });
  protected readonly abreviatura = abreviaturaDocumento;

  protected readonly listado = new ListadoPaginado<Proveedor>(
    (consulta) => this.proveedores.buscar(consulta),
    (e) => this.notificador.error(e, 'No se pudieron cargar los proveedores'),
  );

  protected readonly dialogoAbierto = signal(false);
  protected readonly editando = signal<Proveedor | null>(null);
  protected readonly guardando = signal(false);
  protected readonly consultando = signal(false);

  protected readonly formulario = this.fb.group(
    {
      tipoDocumento: this.fb.control<TipoDocumentoIdentidad>('RUC', Validators.required),
      numeroDocumento: ['', Validators.maxLength(15)],
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      direccion: ['', Validators.maxLength(200)],
      email: ['', [Validators.email, Validators.maxLength(120)]],
      telefono: ['', Validators.maxLength(20)],
    },
    { validators: documentoSegunTipo() },
  );

  private readonly documento = toSignal(
    this.formulario.valueChanges.pipe(
      startWith(null),
      map(() => this.formulario.getRawValue()),
    ),
    { requireSync: true },
  );

  protected readonly consultable = computed(() =>
    documentoConsultable(this.documento().tipoDocumento, this.documento().numeroDocumento),
  );

  protected errorDocumento(): string | null {
    const error = this.formulario.errors?.['documento'] as ErrorDocumento | undefined;
    const numero = this.formulario.controls.numeroDocumento;
    return error && (numero.touched || numero.dirty) ? MENSAJES_DOCUMENTO[error] : null;
  }

  protected invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.dialogoAbierto.set(true);
  }

  protected editar(proveedor: Proveedor): void {
    this.editando.set(proveedor);
    this.formulario.reset({
      tipoDocumento: proveedor.tipoDocumento,
      numeroDocumento: proveedor.numeroDocumento === '-' ? '' : proveedor.numeroDocumento,
      nombre: proveedor.nombre,
      direccion: proveedor.direccion ?? '',
      email: proveedor.email ?? '',
      telefono: proveedor.telefono ?? '',
    });
    this.dialogoAbierto.set(true);
  }

  protected consultarPadron(): void {
    const tipo = this.consultable();
    if (!tipo) {
      return;
    }
    this.consultando.set(true);
    this.padron.consultar(tipo, this.formulario.controls.numeroDocumento.value.trim()).subscribe({
      next: (datos) => {
        this.consultando.set(false);
        this.formulario.patchValue({
          nombre: datos.nombre,
          direccion: datos.direccion ?? this.formulario.controls.direccion.value,
        });
        this.formulario.markAsDirty();
      },
      error: (e: unknown) => {
        this.consultando.set(false);
        this.notificador.error(e, 'No se pudo consultar el documento');
      },
    });
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const proveedor = sinVacios(this.formulario.getRawValue()) as DatosProveedor;
    const actual = this.editando();
    this.guardando.set(true);
    const solicitud = actual
      ? this.proveedores.actualizar(actual.id, proveedor)
      : this.proveedores.crear(proveedor);
    solicitud.subscribe({
      next: (guardado) => {
        this.guardando.set(false);
        this.dialogoAbierto.set(false);
        this.notificador.exito(`Proveedor ${guardado.nombre} guardado`);
        this.listado.recargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar el proveedor');
      },
    });
  }

  protected eliminar(proveedor: Proveedor): void {
    this.notificador.confirmarEliminacion(`al proveedor ${proveedor.nombre}`, () =>
      this.proveedores.eliminar(proveedor.id).subscribe({
        next: () => {
          this.notificador.exito('Proveedor eliminado');
          this.listado.recargar();
        },
        error: (e: unknown) => this.notificador.error(e, 'No se pudo eliminar el proveedor'),
      }),
    );
  }
}
