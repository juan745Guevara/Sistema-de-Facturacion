import { signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, Subject, catchError, debounceTime, distinctUntilChanged, finalize, map, merge, switchMap } from 'rxjs';

import { ConsultaPaginada, Pagina } from '../../core/api/pagina';

export interface EventoPagina {
  first?: number | null;
  rows?: number | null;
}

/**
 * Estado de una tabla paginada en el servidor con búsqueda por texto.
 * Debe crearse en un contexto de inyección (p. ej. como campo de un componente).
 */
export class ListadoPaginado<T> {
  readonly filas = signal<T[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly primero = signal(0);
  readonly tamanio = signal(10);

  private texto = '';
  private readonly textos = new Subject<string>();
  private readonly recargas = new Subject<void>();

  constructor(cargar: (consulta: ConsultaPaginada) => Observable<Pagina<T>>, alFallar: (error: unknown) => void) {
    const busquedas = this.textos.pipe(
      map((t) => t.trim()),
      debounceTime(300),
      distinctUntilChanged(),
      map((t) => {
        this.texto = t;
        this.primero.set(0);
      }),
    );
    merge(busquedas, this.recargas)
      .pipe(
        switchMap(() => {
          this.cargando.set(true);
          const consulta = { q: this.texto, pagina: Math.floor(this.primero() / this.tamanio()), tamanio: this.tamanio() };
          return cargar(consulta).pipe(
            catchError((e: unknown) => {
              alFallar(e);
              return EMPTY;
            }),
            finalize(() => this.cargando.set(false)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((pagina) => {
        this.filas.set(pagina.contenido);
        this.total.set(pagina.totalElementos);
      });
  }

  buscar(texto: string): void {
    this.textos.next(texto);
  }

  cambiarPagina(evento: EventoPagina): void {
    this.primero.set(evento.first ?? 0);
    this.tamanio.set(evento.rows ?? this.tamanio());
    this.recargar();
  }

  /** Vuelve a la primera página, por ejemplo al cambiar un filtro. */
  reiniciar(): void {
    this.primero.set(0);
    this.recargar();
  }

  recargar(): void {
    this.recargas.next();
  }
}
