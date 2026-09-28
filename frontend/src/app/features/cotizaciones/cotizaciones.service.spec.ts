import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CotizacionesService } from './cotizaciones.service';

describe('CotizacionesService', () => {
  let servicio: CotizacionesService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(CotizacionesService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('emite la cotización al backend', () => {
    const recibido = vi.fn();
    servicio.emitir({ serie: 'C001', clienteId: 1, items: [] }).subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/cotizaciones`);
    expect(req.request.body.serie).toBe('C001');
    req.flush({ id: 1, serie: 'C001', correlativo: 1, total: 118 });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
