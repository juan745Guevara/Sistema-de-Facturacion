import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('nueva', 'Nueva compra', 'Fase 5'),
  {
    path: 'proveedores',
    title: 'Proveedores',
    loadComponent: () => import('./proveedores/proveedores.page').then((m) => m.ProveedoresPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'nueva' },
] satisfies Routes;
