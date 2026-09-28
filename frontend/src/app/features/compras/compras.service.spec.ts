import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ComprasService } from './compras.service';

describe('ComprasService', () => {
  let servicio: ComprasService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(ComprasService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('registra la compra en el backend', () => {
    const recibido = vi.fn();
    servicio.registrar({ tipo: 'FACTURA', serie: 'F001', correlativo: '10', proveedorId: 1, items: [] }).subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/compras`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, serie: 'F001', correlativo: '10', total: 20, anulada: false });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
