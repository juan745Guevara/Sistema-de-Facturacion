import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { GuiasService } from './guias.service';

describe('GuiasService', () => {
  let servicio: GuiasService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(GuiasService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('emite la guía al backend', () => {
    const recibido = vi.fn();
    servicio.emitir({ serie: 'T001', clienteId: 1, items: [] }).subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/guias`);
    expect(req.request.body.serie).toBe('T001');
    req.flush({ id: 1, serie: 'T001', correlativo: 1, estadoSunat: 'PENDIENTE' });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
