/** `LocalDate` de la API (AAAA-MM-DD) usando la fecha local, sin desfase por zona horaria. */
export function aFechaIso(fecha: Date | null): string | null {
  if (!fecha) {
    return null;
  }
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function desdeFechaIso(valor: string | null): Date | null {
  const partes = valor?.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  return partes ? new Date(Number(partes[1]), Number(partes[2]) - 1, Number(partes[3])) : null;
}
