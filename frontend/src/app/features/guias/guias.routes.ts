import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('', 'Guías de remisión', 'Fase 5'),
  rutaPendiente('nueva', 'Crear guía de remisión', 'Fase 5'),
] satisfies Routes;
