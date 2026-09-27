import { Route } from '@angular/router';

export function rutaPendiente(path: string, titulo: string, fase: string): Route {
  return {
    path,
    title: titulo,
    loadComponent: () =>
      import('./pagina-pendiente.component').then((m) => m.PaginaPendienteComponent),
    data: { titulo, fase },
  };
}
