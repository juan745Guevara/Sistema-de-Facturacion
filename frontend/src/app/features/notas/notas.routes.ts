import { Routes } from '@angular/router';

export default [
  {
    path: 'credito',
    title: 'Emitir nota de crédito',
    loadComponent: () => import('./emitir/emitir-nota.page').then((m) => m.EmitirNotaPage),
    data: { tipo: 'NOTA_CREDITO' },
  },
  {
    path: 'debito',
    title: 'Emitir nota de débito',
    loadComponent: () => import('./emitir/emitir-nota.page').then((m) => m.EmitirNotaPage),
    data: { tipo: 'NOTA_DEBITO' },
  },
  { path: '', pathMatch: 'full', redirectTo: 'credito' },
] satisfies Routes;
