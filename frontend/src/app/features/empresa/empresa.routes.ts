import { Routes } from '@angular/router';

export default [
  { path: '', title: 'Empresa', loadComponent: () => import('./empresa.page').then((m) => m.EmpresaPage) },
] satisfies Routes;
