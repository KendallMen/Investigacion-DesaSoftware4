import { HttpErrorResponse } from '@angular/common/http';

/**
 * Traduce un error HTTP al texto que se le muestra al usuario.
 * Los microservicios responden { status, mensaje }; por ejemplo el fallback de
 * ms-reservas ("Servicio de laboratorios no disponible, intente más tarde") o
 * la regla del ConfigMap ("La reserva no puede superar las 2 horas").
 */
export function mensajeDeError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const cuerpo = err.error;
    if (cuerpo && typeof cuerpo === 'object' && typeof cuerpo.mensaje === 'string') {
      return cuerpo.mensaje;
    }
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor.';
    }
    if (err.status === 502 || err.status === 503 || err.status === 504) {
      return `El servicio no está disponible en este momento (${err.status}). Intente más tarde.`;
    }
    return `Error ${err.status}: ${err.statusText || 'respuesta inesperada del servidor'}`;
  }
  return 'Ocurrió un error inesperado.';
}
