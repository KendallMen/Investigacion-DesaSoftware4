import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { Observable, catchError, filter, forkJoin, map, of } from 'rxjs';

import { InfoPod } from '../../models';
import { LaboratorioService } from '../../services/laboratorio.service';
import { ReservaService } from '../../services/reserva.service';
import { UsuarioService } from '../../services/usuario.service';

interface EstadoPod {
  servicio: string;
  pod: string | null; // null = el servicio no respondió
}

/**
 * Pie de página que llama a /info de cada microservicio y muestra qué Pod respondió.
 * Se actualiza al cambiar de pantalla y con el botón, para que en la demostración
 * se vea cómo el Service de Kubernetes reparte las peticiones entre las réplicas.
 */
@Component({
  selector: 'app-pie-pod',
  templateUrl: './pie-pod.html',
})
export class PiePod implements OnInit {
  private readonly usuarios = inject(UsuarioService);
  private readonly laboratorios = inject(LaboratorioService);
  private readonly reservas = inject(ReservaService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly estados = signal<EstadoPod[]>([]);
  protected readonly actualizado = signal<string>('');

  ngOnInit(): void {
    this.actualizar();
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.actualizar());
  }

  protected actualizar(): void {
    forkJoin([
      this.consultar('ms-usuarios', this.usuarios.info()),
      this.consultar('ms-laboratorios', this.laboratorios.info()),
      this.consultar('ms-reservas', this.reservas.info()),
    ]).subscribe((estados) => {
      this.estados.set(estados);
      this.actualizado.set(new Date().toLocaleTimeString());
    });
  }

  private consultar(servicio: string, peticion: Observable<InfoPod>): Observable<EstadoPod> {
    return peticion.pipe(
      map((info) => ({ servicio, pod: info.pod })),
      catchError(() => of({ servicio, pod: null })),
    );
  }
}
