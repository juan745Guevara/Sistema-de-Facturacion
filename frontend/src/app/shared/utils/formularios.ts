type SinVacios<T> = { [K in keyof T]: T[K] extends string ? string | null : T[K] };

/** Convierte los textos vacíos o de solo espacios en `null` y recorta el resto. */
export function sinVacios<T extends object>(valores: T): SinVacios<T> {
  return Object.fromEntries(
    Object.entries(valores).map(([clave, valor]) => {
      if (typeof valor !== 'string') {
        return [clave, valor];
      }
      const recortado = valor.trim();
      return [clave, recortado === '' ? null : recortado];
    }),
  ) as SinVacios<T>;
}
