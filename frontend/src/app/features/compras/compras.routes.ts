import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('nueva', 'Nueva compra', 'Fase 5'),
  { path: '', pathMatch: 'full', redirectTo: 'nueva' },
] satisfies Routes;
