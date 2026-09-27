import { Routes } from '@angular/router';

export default [
  { path: '', title: 'Usuarios', loadComponent: () => import('./usuarios.page').then((m) => m.UsuariosPage) },
] satisfies Routes;
