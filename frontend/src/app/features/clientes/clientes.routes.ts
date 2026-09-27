import { Routes } from '@angular/router';

export default [
  { path: '', title: 'Clientes', loadComponent: () => import('./clientes.page').then((m) => m.ClientesPage) },
] satisfies Routes;
