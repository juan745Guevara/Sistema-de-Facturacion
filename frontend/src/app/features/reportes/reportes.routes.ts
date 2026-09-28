import { Routes } from '@angular/router';

export default [
  {
    path: 'ventas',
    title: 'Reporte de ventas',
    loadComponent: () => import('./reporte.page').then((m) => m.ReportePage),
    data: { tipo: 'ventas' },
  },
  {
    path: 'compras',
    title: 'Reporte de compras',
    loadComponent: () => import('./reporte.page').then((m) => m.ReportePage),
    data: { tipo: 'compras' },
  },
  { path: '', pathMatch: 'full', redirectTo: 'ventas' },
] satisfies Routes;
