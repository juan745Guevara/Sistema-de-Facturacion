import { Routes } from '@angular/router';

export default [
  {
    path: 'productos',
    title: 'Almacén de productos',
    loadComponent: () => import('./productos/productos.page').then((m) => m.ProductosPage),
  },
  {
    path: 'categorias',
    title: 'Categorías',
    loadComponent: () => import('./categorias/categorias.page').then((m) => m.CategoriasPage),
  },
  {
    path: 'unidades',
    title: 'Unidades de medida',
    loadComponent: () => import('./unidades/unidades.page').then((m) => m.UnidadesPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'productos' },
] satisfies Routes;
