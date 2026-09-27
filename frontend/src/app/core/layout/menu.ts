import { MenuItem } from 'primeng/api';

import { Rol } from '../auth/auth.models';

interface OpcionMenu {
  label: string;
  icon: string;
  routerLink?: string;
  roles?: Rol[];
  items?: OpcionMenu[];
}

const SOLO_ADMIN: Rol[] = ['ADMINISTRADOR'];

/** Mismas secciones que el menú lateral del sistema PHP. */
const OPCIONES: OpcionMenu[] = [
  { label: 'Inicio', icon: 'pi pi-home', routerLink: '/inicio' },
  {
    label: 'Productos y servicios',
    icon: 'pi pi-box',
    items: [
      { label: 'Almacén de productos', icon: 'pi pi-list', routerLink: '/catalogo/productos' },
      { label: 'Categorías', icon: 'pi pi-tags', routerLink: '/catalogo/categorias' },
      { label: 'Unidades de medida', icon: 'pi pi-sliders-h', routerLink: '/catalogo/unidades' },
    ],
  },
  {
    label: 'Ventas',
    icon: 'pi pi-shopping-cart',
    items: [
      { label: 'Emitir factura', icon: 'pi pi-file', routerLink: '/ventas/factura' },
      { label: 'Emitir boleta', icon: 'pi pi-file', routerLink: '/ventas/boleta' },
      { label: 'Emitir nota de venta', icon: 'pi pi-file', routerLink: '/ventas/nota-venta' },
      { label: 'Emitir nota de crédito', icon: 'pi pi-file-minus', routerLink: '/notas/credito' },
      { label: 'Emitir nota de débito', icon: 'pi pi-file-plus', routerLink: '/notas/debito' },
    ],
  },
  {
    label: 'Compras',
    icon: 'pi pi-inbox',
    items: [{ label: 'Nueva compra', icon: 'pi pi-plus', routerLink: '/compras/nueva' }],
  },
  {
    label: 'Guías de remisión',
    icon: 'pi pi-truck',
    items: [
      { label: 'Crear guía', icon: 'pi pi-plus', routerLink: '/guias/nueva' },
      { label: 'Listar guías', icon: 'pi pi-list', routerLink: '/guias' },
    ],
  },
  {
    label: 'Cotizaciones',
    icon: 'pi pi-calculator',
    items: [
      { label: 'Crear cotización', icon: 'pi pi-plus', routerLink: '/cotizaciones/nueva' },
      { label: 'Listar cotizaciones', icon: 'pi pi-list', routerLink: '/cotizaciones' },
    ],
  },
  {
    label: 'Reportes',
    icon: 'pi pi-chart-bar',
    items: [
      { label: 'Reporte de ventas', icon: 'pi pi-chart-line', routerLink: '/reportes/ventas' },
      { label: 'Reporte de compras', icon: 'pi pi-chart-line', routerLink: '/reportes/compras' },
    ],
  },
  { label: 'Clientes', icon: 'pi pi-users', routerLink: '/clientes' },
  {
    label: 'SUNAT',
    icon: 'pi pi-send',
    items: [
      { label: 'Consultar comprobantes', icon: 'pi pi-search', routerLink: '/sunat/consulta' },
      { label: 'Estados SUNAT', icon: 'pi pi-check-circle', routerLink: '/sunat/estados' },
      { label: 'Resumen diario de boletas', icon: 'pi pi-calendar', routerLink: '/sunat/resumen-diario' },
    ],
  },
  { label: 'Usuarios', icon: 'pi pi-user-edit', routerLink: '/usuarios', roles: SOLO_ADMIN },
  {
    label: 'Configuración',
    icon: 'pi pi-cog',
    roles: SOLO_ADMIN,
    items: [{ label: 'Empresa', icon: 'pi pi-building', routerLink: '/empresa' }],
  },
];

export function menuPara(rol: Rol | undefined): MenuItem[] {
  return filtrar(OPCIONES, rol);
}

function filtrar(opciones: OpcionMenu[], rol: Rol | undefined): MenuItem[] {
  return opciones
    .filter((o) => !o.roles || (rol !== undefined && o.roles.includes(rol)))
    .map(({ roles: _roles, items, ...resto }) => (items ? { ...resto, items: filtrar(items, rol) } : resto));
}
