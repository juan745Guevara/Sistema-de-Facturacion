import { TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';

import { ConsultaPaginada, Pagina } from '../../core/api/pagina';
import { ListadoPaginado } from './listado-paginado';

describe('ListadoPaginado', () => {
  const pagina = (contenido: string[], totalElementos: number): Pagina<string> => ({
    contenido,
    pagina: 0,
    tamanio: 10,
    totalElementos,
  });

  function crear(cargar: (c: ConsultaPaginada) => Observable<Pagina<string>>, alFallar = vi.fn()) {
    return TestBed.runInInjectionContext(() => new ListadoPaginado<string>(cargar, alFallar));
  }

  afterEach(() => vi.useRealTimers());

  it('pide la página según el evento de la tabla', () => {
    const cargar = vi.fn().mockReturnValue(of(pagina(['a', 'b'], 42)));
    const listado = crear(cargar);

    listado.cambiarPagina({ first: 20, rows: 10 });

    expect(cargar).toHaveBeenCalledWith({ q: '', pagina: 2, tamanio: 10 });
    expect(listado.filas()).toEqual(['a', 'b']);
    expect(listado.total()).toBe(42);
    expect(listado.cargando()).toBe(false);
  });

  it('espera a que el usuario deje de escribir y vuelve a la primera página', () => {
    vi.useFakeTimers();
    const cargar = vi.fn().mockReturnValue(of(pagina([], 0)));
    const listado = crear(cargar);
    listado.cambiarPagina({ first: 30, rows: 10 });
    cargar.mockClear();

    listado.buscar('ca');
    listado.buscar('casa ');
    vi.advanceTimersByTime(300);

    expect(cargar).toHaveBeenCalledOnce();
    expect(cargar).toHaveBeenCalledWith({ q: 'casa', pagina: 0, tamanio: 10 });
  });

  it('descarta la respuesta anterior si llega una nueva petición', () => {
    const primera = new Subject<Pagina<string>>();
    const cargar = vi.fn().mockReturnValueOnce(primera).mockReturnValueOnce(of(pagina(['nueva'], 1)));
    const listado = crear(cargar);

    listado.recargar();
    listado.recargar();
    primera.next(pagina(['vieja'], 1));

    expect(listado.filas()).toEqual(['nueva']);
  });

  it('avisa del error y sigue funcionando', () => {
    const alFallar = vi.fn();
    const cargar = vi
      .fn()
      .mockReturnValueOnce(throwError(() => new Error('caído')))
      .mockReturnValueOnce(of(pagina(['ok'], 1)));
    const listado = crear(cargar, alFallar);

    listado.recargar();
    listado.recargar();

    expect(alFallar).toHaveBeenCalledOnce();
    expect(listado.filas()).toEqual(['ok']);
    expect(listado.cargando()).toBe(false);
  });
});
