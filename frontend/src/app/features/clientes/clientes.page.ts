import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';
import { map, startWith } from 'rxjs';

import { CatalogosSunatService, TipoDocumentoIdentidad } from '../../core/api/catalogos-sunat.service';
import { Notificador } from '../../core/api/notificador.service';
import { PadronService } from '../../core/api/padron.service';
import { AuthService } from '../../core/auth/auth.service';
import { abreviaturaDocumento, documentoConsultable } from '../../shared/utils/documentos';
import { aFechaIso, desdeFechaIso } from '../../shared/utils/fechas';
import { sinVacios } from '../../shared/utils/formularios';
import { ListadoPaginado } from '../../shared/utils/listado-paginado';
import { ErrorDocumento, MENSAJES_DOCUMENTO, documentoSegunTipo } from '../../shared/validators/documento.validators';
import { Cliente, ClientesService, DatosCliente } from './clientes.service';

@Component({
  selector: 'app-clientes',
  imports: [ReactiveFormsModule, Button, DatePicker, Dialog, IconField, InputIcon, InputText, Select, TableModule, Tooltip],
  templateUrl: './clientes.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientesPage {
  private readonly clientes = inject(ClientesService);
  private readonly padron = inject(PadronService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly puedeEliminar = inject(AuthService).tieneRol('ADMINISTRADOR', 'ESPECIAL');
  protected readonly tiposDocumento = toSignal(inject(CatalogosSunatService).tiposDocumentoIdentidad$, {
    initialValue: [],
  });
  protected readonly abreviatura = abreviaturaDocumento;
  protected readonly hoy = new Date();

  protected readonly listado = new ListadoPaginado<Cliente>(
    (consulta) => this.clientes.buscar(consulta),
    (e) => this.notificador.error(e, 'No se pudieron cargar los clientes'),
  );

  protected readonly dialogoAbierto = signal(false);
  protected readonly editando = signal<Cliente | null>(null);
  protected readonly guardando = signal(false);
  protected readonly consultando = signal(false);

  protected readonly formulario = this.fb.group(
    {
      tipoDocumento: this.fb.control<TipoDocumentoIdentidad>('DNI', Validators.required),
      numeroDocumento: ['', Validators.maxLength(15)],
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      direccion: ['', Validators.maxLength(200)],
      email: ['', [Validators.email, Validators.maxLength(120)]],
      telefono: ['', Validators.maxLength(20)],
      fechaNacimiento: this.fb.control<Date | null>(null),
    },
    { validators: documentoSegunTipo() },
  );

  private readonly documento = toSignal(
    this.formulario.valueChanges.pipe(
      startWith(null),
      map(() => {
        const { tipoDocumento, numeroDocumento } = this.formulario.getRawValue();
        return { tipoDocumento, numeroDocumento };
      }),
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

  protected editar(cliente: Cliente): void {
    this.editando.set(cliente);
    this.formulario.reset({
      tipoDocumento: cliente.tipoDocumento,
      numeroDocumento: cliente.numeroDocumento === '-' ? '' : cliente.numeroDocumento,
      nombre: cliente.nombre,
      direccion: cliente.direccion ?? '',
      email: cliente.email ?? '',
      telefono: cliente.telefono ?? '',
      fechaNacimiento: desdeFechaIso(cliente.fechaNacimiento),
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
        if (datos.condicion && datos.condicion !== 'HABIDO') {
          this.notificador.aviso(`El contribuyente figura como ${datos.condicion} en SUNAT`);
        }
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
    const { fechaNacimiento, ...resto } = this.formulario.getRawValue();
    const cliente: DatosCliente = {
      ...(sinVacios(resto) as Omit<DatosCliente, 'fechaNacimiento'>),
      fechaNacimiento: aFechaIso(fechaNacimiento),
    };
    const actual = this.editando();
    this.guardando.set(true);
    const solicitud = actual ? this.clientes.actualizar(actual.id, cliente) : this.clientes.crear(cliente);
    solicitud.subscribe({
      next: (guardado) => {
        this.guardando.set(false);
        this.dialogoAbierto.set(false);
        this.notificador.exito(`Cliente ${guardado.nombre} guardado`);
        this.listado.recargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar el cliente');
      },
    });
  }

  protected eliminar(cliente: Cliente): void {
    this.notificador.confirmarEliminacion(`al cliente ${cliente.nombre}`, () =>
      this.clientes.eliminar(cliente.id).subscribe({
        next: () => {
          this.notificador.exito('Cliente eliminado');
          this.listado.recargar();
        },
        error: (e: unknown) => this.notificador.error(e, 'No se pudo eliminar el cliente'),
      }),
    );
  }
}
