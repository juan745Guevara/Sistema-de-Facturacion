import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { VentasService } from './ventas.service';

describe('VentasService', () => {
  let servicio: VentasService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(VentasService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('pide el recálculo al backend', () => {
    const recibido = vi.fn();
    servicio.previsualizar([{ productoId: 1, cantidad: 2 }]).subscribe(recibido);

    const req = backend.expectOne(`${environment.apiUrl}/ventas/previsualizar`);
    expect(req.request.body).toEqual({
      items: [{ productoId: 1, cantidad: 2 }],
      moneda: 'PEN',
      tipoCambio: undefined,
    });
    req.flush({ lineas: [], totales: { total: 10 } });
    expect(recibido).toHaveBeenCalledOnce();
  });
});
