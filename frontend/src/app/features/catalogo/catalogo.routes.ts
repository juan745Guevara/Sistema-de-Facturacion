import { Routes } from '@angular/router';

import { rutaPendiente } from '../../shared/components/pagina-pendiente/ruta-pendiente';

export default [
  rutaPendiente('productos', 'Almacén de productos', 'Fase 2'),
  rutaPendiente('categorias', 'Categorías', 'Fase 2'),
  rutaPendiente('unidades', 'Unidades de medida', 'Fase 2'),
  { path: '', pathMatch: 'full', redirectTo: 'productos' },
] satisfies Routes;
