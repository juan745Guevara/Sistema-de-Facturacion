import { Routes } from '@angular/router';

export default [
  {
    path: 'estados',
    title: 'Estados SUNAT',
    loadComponent: () => import('./estados/estados.page').then((m) => m.EstadosSunatPage),
  },
  {
    path: 'consulta',
    title: 'Consultar comprobantes',
    loadComponent: () => import('./consulta/consulta.page').then((m) => m.ConsultaSunatPage),
  },
  {
    path: 'resumen-diario',
    title: 'Resumen diario de boletas',
    loadComponent: () => import('./resumen/resumen-diario.page').then((m) => m.ResumenDiarioPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'estados' },
] satisfies Routes;
