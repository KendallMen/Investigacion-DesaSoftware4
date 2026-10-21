import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { InfoPod, Laboratorio } from '../models';

@Injectable({ providedIn: 'root' })
export class LaboratorioService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/laboratorios';

  listar(): Observable<Laboratorio[]> {
    return this.http.get<Laboratorio[]>(this.url);
  }

  crear(laboratorio: Laboratorio): Observable<Laboratorio> {
    return this.http.post<Laboratorio>(this.url, laboratorio);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  info(): Observable<InfoPod> {
    return this.http.get<InfoPod>(`${this.url}/info`);
  }
}
