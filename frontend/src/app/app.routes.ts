import { Routes } from '@angular/router';

import { authGuard, invitadoGuard, rolGuard } from './core/auth/auth.guards';
import { Rol } from './core/auth/auth.models';
const SOLO_ADMIN: Rol[] = ['ADMINISTRADOR'];

export const routes: Routes = [
  {
    path: 'login',
    title: 'Iniciar sesión',
    canMatch: [invitadoGuard],
    loadComponent: () => import('./core/auth/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: '',
    loadComponent: () => import('./core/layout/shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    canActivateChild: [rolGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', loadChildren: () => import('./features/inicio/inicio.routes') },
      { path: 'catalogo', loadChildren: () => import('./features/catalogo/catalogo.routes') },
      { path: 'clientes', loadChildren: () => import('./features/clientes/clientes.routes') },
      { path: 'ventas', loadChildren: () => import('./features/ventas/ventas.routes') },
      { path: 'notas', loadChildren: () => import('./features/notas/notas.routes') },
      { path: 'guias', loadChildren: () => import('./features/guias/guias.routes') },
      { path: 'compras', loadChildren: () => import('./features/compras/compras.routes') },
      { path: 'cotizaciones', loadChildren: () => import('./features/cotizaciones/cotizaciones.routes') },
      { path: 'sunat', loadChildren: () => import('./features/sunat/sunat.routes') },
      { path: 'reportes', loadChildren: () => import('./features/reportes/reportes.routes') },
      {
        path: 'empresa',
        data: { roles: SOLO_ADMIN },
        loadChildren: () => import('./features/empresa/empresa.routes'),
      },
      {
        path: 'usuarios',
        data: { roles: SOLO_ADMIN },
        loadChildren: () => import('./features/usuarios/usuarios.routes'),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
