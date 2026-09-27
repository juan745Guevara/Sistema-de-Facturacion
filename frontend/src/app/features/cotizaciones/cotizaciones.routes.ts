import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('', 'Cotizaciones', 'Fase 5'),
  rutaPendiente('nueva', 'Crear cotización', 'Fase 5'),
] satisfies Routes;
