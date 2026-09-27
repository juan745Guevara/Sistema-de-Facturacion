import { Pipe, PipeTransform } from '@angular/core';

export type Moneda = 'PEN' | 'USD';

const SIMBOLOS: Record<Moneda, string> = { PEN: 'S/', USD: 'US$' };
const FORMATO = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** Solo para mostrar. Los montos definitivos los calcula y redondea el backend. */
@Pipe({ name: 'monto' })
export class MontoPipe implements PipeTransform {
  transform(valor: number | string | null | undefined, moneda: Moneda = 'PEN'): string {
    if (valor === null || valor === undefined || valor === '') {
      return '';
    }
    const numero = typeof valor === 'number' ? valor : Number(valor);
    return Number.isFinite(numero) ? `${SIMBOLOS[moneda]} ${FORMATO.format(numero)}` : '';
  }
}
