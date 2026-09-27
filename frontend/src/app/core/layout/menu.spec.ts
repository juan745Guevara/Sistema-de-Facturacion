import { MenuItem } from 'primeng/api';

import { menuPara } from './menu';

function etiquetas(items: MenuItem[]): string[] {
  return items.map((i) => i.label ?? '');
}

describe('menuPara', () => {
  it('muestra usuarios y configuración solo al administrador', () => {
    expect(etiquetas(menuPara('ADMINISTRADOR'))).toEqual(expect.arrayContaining(['Usuarios', 'Configuración']));
    expect(etiquetas(menuPara('VENDEDOR'))).not.toContain('Usuarios');
    expect(etiquetas(menuPara('ESPECIAL'))).not.toContain('Configuración');
  });

  it('sin rol no muestra opciones restringidas', () => {
    expect(etiquetas(menuPara(undefined))).not.toContain('Usuarios');
  });

  it('conserva los submenús y no expone la lista de roles', () => {
    const ventas = menuPara('VENDEDOR').find((i) => i.label === 'Ventas');
    expect(ventas?.items?.map((i) => i.routerLink)).toContain('/ventas/factura');
    expect(menuPara('ADMINISTRADOR').some((i) => 'roles' in i)).toBe(false);
  });
});
