import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('ventas', 'Reporte de ventas', 'Fase 6'),
  rutaPendiente('compras', 'Reporte de compras', 'Fase 6'),
  { path: '', pathMatch: 'full', redirectTo: 'ventas' },
] satisfies Routes;
