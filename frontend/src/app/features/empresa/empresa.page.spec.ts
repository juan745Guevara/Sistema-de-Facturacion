import { HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { boton, elemento, enviar, escribir, proveedoresDePagina } from '../../shared/testing/pruebas';
import { EmpresaPage } from './empresa.page';

const API = environment.apiUrl;

describe('EmpresaPage', () => {
  let fixture: ComponentFixture<EmpresaPage>;
  let backend: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({ imports: [EmpresaPage], providers: proveedoresDePagina('ADMINISTRADOR') });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(EmpresaPage);
    await fixture.whenStable();
    backend
      .expectOne(`${API}/empresa`)
      .flush({ codigo: 'empresa-no-configurada' }, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();
  });

  afterEach(() => backend.verify());

  const valor = (selector: string) => elemento<HTMLInputElement>(fixture, selector)!.value;

  it('avisa que falta configurar y propone el IGV y el establecimiento por defecto', () => {
    expect(fixture.nativeElement.textContent).toContain('aún no está configurada');
    expect(valor('#codigoEstablecimiento')).toBe('0000');
    expect(valor('#codigoPais')).toBe('PE');
  });

  it('completa los datos desde SUNAT y guarda la empresa', async () => {
    escribir(fixture, '#ruc', '20601487871');
    boton(fixture, 'Consultar RUC en SUNAT')!.click();
    backend.expectOne(`${API}/documentos-identidad/RUC/20601487871`).flush({
      tipoDocumento: 'RUC',
      numeroDocumento: '20601487871',
      nombre: 'EMPRESA DEMO S.A.C.',
      direccion: 'AV. AREQUIPA 123',
      ubigeo: '150101',
      departamento: 'LIMA',
      provincia: 'LIMA',
      distrito: 'LIMA',
      estado: 'ACTIVO',
      condicion: 'HABIDO',
    });
    await fixture.whenStable();

    expect(valor('#razonSocial')).toBe('EMPRESA DEMO S.A.C.');
    expect(valor('#ubigeo')).toBe('150101');

    await enviar(fixture);
    const guardado = backend.expectOne({ url: `${API}/empresa`, method: 'PUT' });
    expect(guardado.request.body).toMatchObject({
      ruc: '20601487871',
      razonSocial: 'EMPRESA DEMO S.A.C.',
      nombreComercial: null,
      porcentajeIgv: 18,
      domicilioFiscal: { ubigeo: '150101', codigoEstablecimiento: '0000', codigoPais: 'PE' },
    });
    guardado.flush(guardado.request.body);
  });

  it('no consulta un RUC inválido', () => {
    escribir(fixture, '#ruc', '20601487872');
    boton(fixture, 'Consultar RUC en SUNAT')!.click();

    backend.expectNone((r) => r.url.includes('/documentos-identidad/'));
  });
});
