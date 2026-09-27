import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { EmpresaService } from './empresa.service';

describe('EmpresaService', () => {
  let servicio: EmpresaService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(EmpresaService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('devuelve null mientras la empresa no esté configurada', () => {
    const recibido = vi.fn();
    servicio.obtener().subscribe(recibido);

    backend
      .expectOne(`${environment.apiUrl}/empresa`)
      .flush({ codigo: 'empresa-no-configurada' }, { status: 404, statusText: 'Not Found' });

    expect(recibido).toHaveBeenCalledWith(null);
  });

  it('propaga los demás errores', () => {
    const error = vi.fn();
    servicio.obtener().subscribe({ error });

    backend.expectOne(`${environment.apiUrl}/empresa`).flush(null, { status: 500, statusText: 'Error' });

    expect(error).toHaveBeenCalledOnce();
  });
});
