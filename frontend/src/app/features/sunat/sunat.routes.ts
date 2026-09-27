import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('estados', 'Estados SUNAT', 'Fase 3'),
  rutaPendiente('consulta', 'Consultar comprobantes', 'Fase 3'),
  rutaPendiente('resumen-diario', 'Resumen diario de boletas', 'Fase 4'),
  { path: '', pathMatch: 'full', redirectTo: 'estados' },
] satisfies Routes;
