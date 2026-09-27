import { HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { environment } from '../../../../environments/environment';
import { Rol } from '../../../core/auth/auth.models';
import { boton, enviar, escribir, proveedoresDePagina } from '../../../shared/testing/pruebas';
import { CategoriasPage } from './categorias.page';

const URL = `${environment.apiUrl}/catalogo/categorias`;

describe('CategoriasPage', () => {
  let fixture: ComponentFixture<CategoriasPage>;
  let backend: HttpTestingController;

  async function abrir(rol: Rol): Promise<void> {
    TestBed.configureTestingModule({ imports: [CategoriasPage], providers: proveedoresDePagina(rol) });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(CategoriasPage);
    await fixture.whenStable();
    backend.expectOne(URL).flush([
      { id: 1, nombre: 'BEBIDAS' },
      { id: 2, nombre: 'LIMPIEZA' },
    ]);
    await fixture.whenStable();
  }

  afterEach(() => backend.verify());

  it('el vendedor solo consulta', async () => {
    await abrir('VENDEDOR');

    expect(fixture.nativeElement.textContent).toContain('BEBIDAS');
    expect(boton(fixture, 'Nueva')).toBeNull();
    expect(boton(fixture, 'Eliminar BEBIDAS')).toBeNull();
  });

  it('filtra la lista sin llamar al API', async () => {
    await abrir('VENDEDOR');

    escribir(fixture, 'input[type="search"]', 'limp');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('LIMPIEZA');
    expect(fixture.nativeElement.textContent).not.toContain('BEBIDAS');
  });

  it('el rol especial crea una categoría y recarga la lista', async () => {
    await abrir('ESPECIAL');
    boton(fixture, 'Nueva')!.click();
    await fixture.whenStable();

    escribir(fixture, '#nombreCategoria', '  Ferretería ');
    await enviar(fixture, 'p-dialog form');

    const alta = backend.expectOne({ url: URL, method: 'POST' });
    expect(alta.request.body).toEqual({ nombre: 'Ferretería' });
    alta.flush({ id: 3, nombre: 'FERRETERÍA' });
    backend.expectOne({ url: URL, method: 'GET' }).flush([]);
  });
});
