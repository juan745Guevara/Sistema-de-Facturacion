import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ReportesService } from './reportes.service';

describe('ReportesService', () => {
  let servicio: ReportesService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(ReportesService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('pide el dashboard al backend', () => {
    const recibido = vi.fn();
    servicio.dashboard().subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/reportes/dashboard`);
    req.flush({ ventasHoy: 0, comprobantesPendientes: 0, comprasDelMes: 0, totalComprasMes: 0 });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
