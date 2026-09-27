import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ConsultaPaginada, Pagina, parametros } from '../../core/api/pagina';

export interface Categoria {
  id: number;
  nombre: string;
}

export interface UnidadMedida {
  codigo: string;
  descripcion: string;
  activa: boolean;
}

export interface Producto {
  id: number;
  codigo: string;
  descripcion: string;
  categoriaId: number;
  unidadMedida: string;
  tipoAfectacionIgv: string;
  precioVenta: number;
  precioCompra: number | null;
  stock: number | null;
}

export type DatosProducto = Omit<Producto, 'id'>;

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/catalogo`;

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.url}/categorias`);
  }

  crearCategoria(nombre: string): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.url}/categorias`, { nombre });
  }

  renombrarCategoria(id: number, nombre: string): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/categorias/${id}`, { nombre });
  }

  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/categorias/${id}`);
  }

  listarUnidades(soloActivas = false): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>(`${this.url}/unidades`, { params: parametros({ soloActivas }) });
  }

  cambiarActivacion(codigo: string, activa: boolean): Observable<UnidadMedida> {
    return this.http.patch<UnidadMedida>(`${this.url}/unidades/${encodeURIComponent(codigo)}`, { activa });
  }

  buscarProductos(consulta: ConsultaPaginada & { categoriaId?: number | null }): Observable<Pagina<Producto>> {
    return this.http.get<Pagina<Producto>>(`${this.url}/productos`, { params: parametros({ ...consulta }) });
  }

  crearProducto(producto: DatosProducto): Observable<Producto> {
    return this.http.post<Producto>(`${this.url}/productos`, producto);
  }

  actualizarProducto(id: number, producto: DatosProducto): Observable<Producto> {
    return this.http.put<Producto>(`${this.url}/productos/${id}`, producto);
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/productos/${id}`);
  }
}
