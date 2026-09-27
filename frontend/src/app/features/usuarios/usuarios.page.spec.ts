import { HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { boton, elemento, enviar, escribir, proveedoresDePagina } from '../../shared/testing/pruebas';
import { UsuariosPage } from './usuarios.page';

const URL = `${environment.apiUrl}/usuarios`;

describe('UsuariosPage', () => {
  let fixture: ComponentFixture<UsuariosPage>;
  let backend: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({ imports: [UsuariosPage], providers: proveedoresDePagina('ADMINISTRADOR') });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UsuariosPage);
    await fixture.whenStable();
    backend.expectOne(URL).flush([
      { id: 1, nombre: 'Prueba', username: 'prueba', email: null, rol: 'ADMINISTRADOR', activo: true, ultimoLogin: null },
    ]);
    await fixture.whenStable();
    boton(fixture, 'Nuevo usuario')!.click();
    await fixture.whenStable();
  });

  afterEach(() => backend.verify());

  function completar(password: string, confirmacion: string): void {
    escribir(fixture, '#nuevoNombre', 'Luis Vendedor');
    escribir(fixture, '#nuevoUsername', 'luis');
    escribir(fixture, '#nuevoPassword', password);
    escribir(fixture, '#nuevoConfirmacion', confirmacion);
  }

  it('crea el usuario sin enviar la confirmación de la clave', async () => {
    completar('una-clave-bien-larga', 'una-clave-bien-larga');
    await enviar(fixture, 'p-dialog form');

    const alta = backend.expectOne({ url: URL, method: 'POST' });
    expect(alta.request.body).toEqual({
      nombre: 'Luis Vendedor',
      username: 'luis',
      email: null,
      rol: 'VENDEDOR',
      password: 'una-clave-bien-larga',
    });
    alta.flush({ id: 2 });
    backend.expectOne({ url: URL, method: 'GET' }).flush([]);
  });

  it('exige que las claves coincidan y tengan 12 caracteres', async () => {
    completar('corta', 'distinta');
    await enviar(fixture, 'p-dialog form');

    expect(elemento(fixture, 'p-dialog')!.textContent).toContain('Mínimo 12 caracteres');
    expect(elemento(fixture, 'p-dialog')!.textContent).toContain('Las contraseñas no coinciden');
    backend.expectNone({ url: URL, method: 'POST' });
  });
});
