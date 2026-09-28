import { Routes } from '@angular/router';

export default [
  {
    path: 'nueva',
    title: 'Nueva compra',
    loadComponent: () => import('./nueva/nueva-compra.page').then((m) => m.NuevaCompraPage),
  },
  {
    path: 'proveedores',
    title: 'Proveedores',
    loadComponent: () => import('./proveedores/proveedores.page').then((m) => m.ProveedoresPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'nueva' },
] satisfies Routes;
