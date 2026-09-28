import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { NotasService } from './notas.service';

describe('NotasService', () => {
  let servicio: NotasService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(NotasService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('emite la nota al backend', () => {
    const recibido = vi.fn();
    servicio
      .emitir({
        tipo: 'NOTA_CREDITO',
        serie: 'FC01',
        tipoReferencia: 'FACTURA',
        serieReferencia: 'F001',
        correlativoReferencia: 1,
        codigoMotivo: '01',
      })
      .subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/notas`);
    expect(req.request.body.codigoMotivo).toBe('01');
    req.flush({ id: 1, serie: 'FC01', correlativo: 1 });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
