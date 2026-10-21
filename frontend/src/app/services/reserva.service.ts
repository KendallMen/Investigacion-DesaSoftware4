import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { InfoPod, Reserva, ReservaRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/reservas';

  listar(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(this.url);
  }

  porUsuario(usuarioId: number): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.url}/usuario/${usuarioId}`);
  }

  crear(reserva: ReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(this.url, reserva);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  info(): Observable<InfoPod> {
    return this.http.get<InfoPod>(`${this.url}/info`);
  }
}
