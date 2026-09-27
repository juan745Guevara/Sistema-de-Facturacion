import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';

import { Rol } from './auth.models';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.estaAutenticado()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const invitadoGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  return auth.estaAutenticado() ? inject(Router).createUrlTree(['/inicio']) : true;
};

/** Lee los roles permitidos de `data.roles`. Sin roles declarados, deja pasar. */
export const rolGuard: CanActivateFn = (route) => {
  const roles = (route.data['roles'] as Rol[] | undefined) ?? [];
  if (roles.length === 0 || inject(AuthService).tieneRol(...roles)) {
    return true;
  }
  return inject(Router).createUrlTree(['/inicio']);
};
