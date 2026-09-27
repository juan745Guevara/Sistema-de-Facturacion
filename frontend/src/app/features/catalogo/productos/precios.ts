/** El sistema anterior proponía como precio de compra el 70 % del precio de venta. */
export const FACTOR_PRECIO_COMPRA_SUGERIDO = 0.7;

const TASA_IVAP = 4;

export interface DesglosePrecio {
  valorUnitario: number;
  impuesto: number;
  tasa: number;
}

/**
 * Separa el impuesto incluido en el precio de venta. Solo es una vista previa:
 * los importes de los comprobantes los calcula el backend.
 */
export function desglosarPrecio(precio: number | null, tipoAfectacionIgv: string | null, porcentajeIgv: number): DesglosePrecio | null {
  if (precio === null || !Number.isFinite(precio) || precio <= 0) {
    return null;
  }
  const tasa = tasaPara(tipoAfectacionIgv, porcentajeIgv);
  const valorUnitario = redondear(precio / (1 + tasa / 100));
  return { valorUnitario, impuesto: redondear(precio - valorUnitario), tasa };
}

export function precioCompraSugerido(precioVenta: number | null): number | null {
  return precioVenta === null || precioVenta <= 0 ? null : redondear(precioVenta * FACTOR_PRECIO_COMPRA_SUGERIDO);
}

function tasaPara(tipoAfectacionIgv: string | null, porcentajeIgv: number): number {
  switch (tipoAfectacionIgv) {
    case 'GRAVADO_ONEROSO':
      return porcentajeIgv;
    case 'GRAVADO_IVAP':
      return TASA_IVAP;
    default:
      return 0;
  }
}

function redondear(valor: number): number {
  return Math.round((valor + Number.EPSILON) * 100) / 100;
}
