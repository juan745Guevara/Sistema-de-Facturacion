import { Routes } from '@angular/router';

export default [
  {
    path: '',
    title: 'Cotizaciones',
    loadComponent: () => import('./lista/cotizaciones.page').then((m) => m.CotizacionesPage),
  },
  {
    path: 'nueva',
    title: 'Nueva cotización',
    loadComponent: () => import('./nueva/nueva-cotizacion.page').then((m) => m.NuevaCotizacionPage),
  },
] satisfies Routes;
