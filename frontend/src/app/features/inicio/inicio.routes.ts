import { Routes } from '@angular/router';

export default [
  { path: '', title: 'Inicio', loadComponent: () => import('./inicio.page').then((m) => m.InicioPage) },
] satisfies Routes;
