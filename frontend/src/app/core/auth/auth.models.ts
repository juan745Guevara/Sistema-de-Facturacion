export type Rol = 'ADMINISTRADOR' | 'ESPECIAL' | 'VENDEDOR';

export interface UsuarioSesion {
  id: number;
  nombre: string;
  username: string;
  rol: Rol;
}

export interface Sesion {
  token: string;
  expiraEn: string;
  usuario: UsuarioSesion;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse extends Sesion {
  tipo: 'Bearer';
}
