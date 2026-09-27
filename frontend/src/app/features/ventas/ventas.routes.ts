import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('factura', 'Emitir factura', 'Fase 3'),
  rutaPendiente('boleta', 'Emitir boleta', 'Fase 3'),
  rutaPendiente('nota-venta', 'Emitir nota de venta', 'Fase 3'),
  { path: '', pathMatch: 'full', redirectTo: 'factura' },
] satisfies Routes;
