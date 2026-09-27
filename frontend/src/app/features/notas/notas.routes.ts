import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('credito', 'Emitir nota de crédito', 'Fase 4'),
  rutaPendiente('debito', 'Emitir nota de débito', 'Fase 4'),
  { path: '', pathMatch: 'full', redirectTo: 'credito' },
] satisfies Routes;
