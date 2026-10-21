// Mismos campos que devuelven ms-usuarios, ms-laboratorios y ms-reservas.

export interface Usuario {
  id?: number;
  nombre: string;
  carnet: string;
  correo: string;
  carrera: string;
}

export interface Laboratorio {
  id?: number;
  nombre: string;
  edificio: string;
  capacidad: number | null;
  disponible: boolean;
}

export interface Reserva {
  id?: number;
  usuarioId: number;
  laboratorioId: number;
  fecha: string;      // yyyy-MM-dd
  horaInicio: string; // HH:mm o HH:mm:ss
  horaFin: string;
  estado?: string;
}

export interface ReservaRequest {
  usuarioId: number | null;
  laboratorioId: number | null;
  fecha: string;
  horaInicio: string;
  horaFin: string;
}

/** Respuesta de /api/<servicio>/info */
export interface InfoPod {
  servicio: string;
  pod: string;
}
