import { Routes } from '@angular/router';

export default [
  {
    path: '',
    title: 'Guías de remisión',
    loadComponent: () => import('./lista/guias.page').then((m) => m.GuiasPage),
  },
  {
    path: 'nueva',
    title: 'Nueva guía',
    loadComponent: () => import('./nueva/nueva-guia.page').then((m) => m.NuevaGuiaPage),
  },
] satisfies Routes;
