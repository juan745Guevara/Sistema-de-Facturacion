import { Routes } from '@angular/router';

export default [
  {
    path: 'factura',
    title: 'Emitir factura',
    data: { tipo: 'FACTURA' },
    loadComponent: () => import('./emitir/emitir-comprobante.page').then((m) => m.EmitirComprobantePage),
  },
  {
    path: 'boleta',
    title: 'Emitir boleta',
    data: { tipo: 'BOLETA' },
    loadComponent: () => import('./emitir/emitir-comprobante.page').then((m) => m.EmitirComprobantePage),
  },
  {
    path: 'nota-venta',
    title: 'Emitir nota de venta',
    data: { tipo: 'NOTA_VENTA' },
    loadComponent: () => import('./emitir/emitir-comprobante.page').then((m) => m.EmitirComprobantePage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'factura' },
] satisfies Routes;
