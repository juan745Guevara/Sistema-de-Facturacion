import { HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Rol } from '../../core/auth/auth.models';
import { boton, elemento, enviar, escribir, proveedoresDePagina } from '../../shared/testing/pruebas';
import { ClientesPage } from './clientes.page';

const API = environment.apiUrl;
const TIPOS = [
  { valor: 'DNI', codigo: '1', descripcion: 'DNI' },
  { valor: 'RUC', codigo: '6', descripcion: 'RUC' },
];
const CLIENTE = {
  id: 7,
  tipoDocumento: 'RUC',
  numeroDocumento: '20601487871',
  nombre: 'COMERCIAL ANDINA S.A.C.',
  direccion: 'AV. LIMA 123',
  email: null,
  telefono: null,
  fechaNacimiento: null,
};

describe('ClientesPage', () => {
  let fixture: ComponentFixture<ClientesPage>;
  let backend: HttpTestingController;

  async function abrir(rol: Rol): Promise<void> {
    TestBed.configureTestingModule({ imports: [ClientesPage], providers: proveedoresDePagina(rol) });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ClientesPage);
    await fixture.whenStable();
    backend.expectOne(`${API}/catalogos-sunat/tipos-documento-identidad`).flush(TIPOS);
    responderListado();
    await fixture.whenStable();
  }

  function responderListado(): void {
    backend
      .expectOne((r) => r.url === `${API}/clientes` && r.method === 'GET')
      .flush({ contenido: [CLIENTE], pagina: 0, tamanio: 10, totalElementos: 1 });
  }

  afterEach(() => backend.verify());

  it('muestra los clientes y oculta eliminar al vendedor', async () => {
    await abrir('VENDEDOR');

    expect(fixture.nativeElement.textContent).toContain('COMERCIAL ANDINA S.A.C.');
    expect(boton(fixture, `Editar ${CLIENTE.nombre}`)).not.toBeNull();
    expect(boton(fixture, `Eliminar ${CLIENTE.nombre}`)).toBeNull();
  });

  it('permite eliminar al rol especial', async () => {
    await abrir('ESPECIAL');

    expect(boton(fixture, `Eliminar ${CLIENTE.nombre}`)).not.toBeNull();
  });

  it('completa el nombre con el padrón y registra el cliente', async () => {
    await abrir('VENDEDOR');
    boton(fixture, 'Nuevo')!.click();
    await fixture.whenStable();

    escribir(fixture, '#numeroDocumento', '47204426');
    await fixture.whenStable();
    boton(fixture, 'Consultar en RENIEC o SUNAT')!.click();
    backend
      .expectOne(`${API}/documentos-identidad/DNI/47204426`)
      .flush({ tipoDocumento: 'DNI', numeroDocumento: '47204426', nombre: 'PEREZ GOMEZ ANA', direccion: null });
    await fixture.whenStable();

    expect(elemento<HTMLInputElement>(fixture, '#nombre')!.value).toBe('PEREZ GOMEZ ANA');

    await enviar(fixture, 'p-dialog form');
    const alta = backend.expectOne({ url: `${API}/clientes`, method: 'POST' });
    expect(alta.request.body).toEqual({
      tipoDocumento: 'DNI',
      numeroDocumento: '47204426',
      nombre: 'PEREZ GOMEZ ANA',
      direccion: null,
      email: null,
      telefono: null,
      fechaNacimiento: null,
    });
    alta.flush({ ...CLIENTE, id: 8, nombre: 'PEREZ GOMEZ ANA' });
    responderListado();
  });

  it('no envía un documento inválido', async () => {
    await abrir('VENDEDOR');
    boton(fixture, 'Nuevo')!.click();
    await fixture.whenStable();

    escribir(fixture, '#numeroDocumento', '1234');
    escribir(fixture, '#nombre', 'Ana');
    await enviar(fixture, 'p-dialog form');

    expect(fixture.nativeElement.textContent).toContain('El DNI debe tener 8 dígitos');
    backend.expectNone({ url: `${API}/clientes`, method: 'POST' });
  });
});
