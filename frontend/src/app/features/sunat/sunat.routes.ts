import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

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
  rutaPendiente('resumen-diario', 'Resumen diario de boletas', 'Fase 4'),
  { path: '', pathMatch: 'full', redirectTo: 'estados' },
] satisfies Routes;
