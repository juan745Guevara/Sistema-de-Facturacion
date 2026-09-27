import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Checkbox } from 'primeng/checkbox';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Tooltip } from 'primeng/tooltip';

import { Empresa, EmpresaService, IGV_POR_DEFECTO } from '../../core/api/empresa.service';
import { Notificador } from '../../core/api/notificador.service';
import { PadronService } from '../../core/api/padron.service';
import { sinVacios } from '../../shared/utils/formularios';
import { rucValidator } from '../../shared/validators/documento.validators';

@Component({
  selector: 'app-empresa',
  imports: [ReactiveFormsModule, Button, Card, Checkbox, InputNumber, InputText, Message, Tooltip],
  templateUrl: './empresa.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmpresaPage implements OnInit {
  private readonly empresas = inject(EmpresaService);
  private readonly padron = inject(PadronService);
  private readonly notificador = inject(Notificador);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly consultando = signal(false);
  protected readonly sinConfigurar = signal(false);

  protected readonly formulario = this.fb.group({
    ruc: ['', [Validators.required, rucValidator]],
    razonSocial: ['', [Validators.required, Validators.maxLength(200)]],
    nombreComercial: ['', Validators.maxLength(200)],
    telefono: ['', Validators.maxLength(20)],
    correoVentas: ['', [Validators.email, Validators.maxLength(120)]],
    correoSoporte: ['', [Validators.email, Validators.maxLength(120)]],
    porcentajeIgv: [IGV_POR_DEFECTO, [Validators.required, Validators.min(0), Validators.max(99.99)]],
    bienesSelva: [false],
    serviciosSelva: [false],
    domicilioFiscal: this.fb.group({
      direccion: ['', [Validators.required, Validators.maxLength(200)]],
      ubigeo: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      departamento: ['', [Validators.required, Validators.maxLength(60)]],
      provincia: ['', [Validators.required, Validators.maxLength(60)]],
      distrito: ['', [Validators.required, Validators.maxLength(60)]],
      codigoPais: ['PE', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
      codigoEstablecimiento: ['0000', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    }),
  });

  ngOnInit(): void {
    this.empresas.obtener().subscribe({
      next: (empresa) => {
        this.cargando.set(false);
        this.sinConfigurar.set(empresa === null);
        if (empresa) {
          this.formulario.reset(conTextos(empresa));
        }
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.notificador.error(e, 'No se pudo cargar la empresa');
      },
    });
  }

  protected invalido(ruta: string): boolean {
    const control: AbstractControl | null = this.formulario.get(ruta);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  protected consultarRuc(): void {
    const ruc = this.formulario.controls.ruc;
    ruc.markAsTouched();
    if (ruc.invalid) {
      return;
    }
    this.consultando.set(true);
    this.padron.consultar('RUC', ruc.value.trim()).subscribe({
      next: (datos) => {
        this.consultando.set(false);
        this.formulario.patchValue({
          razonSocial: datos.nombre,
          domicilioFiscal: {
            direccion: datos.direccion ?? '',
            ubigeo: datos.ubigeo ?? '',
            departamento: datos.departamento ?? '',
            provincia: datos.provincia ?? '',
            distrito: datos.distrito ?? '',
          },
        });
        this.formulario.markAsDirty();
        if (datos.estado && datos.estado !== 'ACTIVO') {
          this.notificador.aviso(`El RUC figura como ${datos.estado} en SUNAT`);
        }
      },
      error: (e: unknown) => {
        this.consultando.set(false);
        this.notificador.error(e, 'No se pudo consultar el RUC');
      },
    });
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valores = this.formulario.getRawValue();
    const empresa = {
      ...sinVacios(valores),
      domicilioFiscal: sinVacios(valores.domicilioFiscal),
    } as Empresa;
    this.guardando.set(true);
    this.empresas.guardar(empresa).subscribe({
      next: (guardada) => {
        this.guardando.set(false);
        this.sinConfigurar.set(false);
        this.formulario.reset(conTextos(guardada));
        this.notificador.exito('Datos de la empresa guardados');
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.notificador.error(e, 'No se pudo guardar la empresa');
      },
    });
  }
}

/** El formulario trabaja con textos vacíos en lugar de `null`. */
function conTextos(empresa: Empresa) {
  return {
    ...empresa,
    nombreComercial: empresa.nombreComercial ?? '',
    telefono: empresa.telefono ?? '',
    correoVentas: empresa.correoVentas ?? '',
    correoSoporte: empresa.correoSoporte ?? '',
    domicilioFiscal: {
      ...empresa.domicilioFiscal,
      codigoPais: empresa.domicilioFiscal.codigoPais ?? 'PE',
      codigoEstablecimiento: empresa.domicilioFiscal.codigoEstablecimiento ?? '0000',
    },
  };
}
