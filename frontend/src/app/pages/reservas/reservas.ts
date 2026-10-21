import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Laboratorio, Reserva, ReservaRequest, Usuario } from '../../models';
import { LaboratorioService } from '../../services/laboratorio.service';
import { mensajeDeError } from '../../services/mensaje-error';
import { ReservaService } from '../../services/reserva.service';
import { UsuarioService } from '../../services/usuario.service';

function manana(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function formularioVacio(): ReservaRequest {
  return { usuarioId: null, laboratorioId: null, fecha: manana(), horaInicio: '08:00', horaFin: '10:00' };
}

@Component({
  selector: 'app-reservas',
  imports: [FormsModule],
  templateUrl: './reservas.html',
})
export class Reservas implements OnInit {
  private readonly reservaService = inject(ReservaService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly laboratorioService = inject(LaboratorioService);

  protected readonly reservas = signal<Reserva[]>([]);
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly laboratorios = signal<Laboratorio[]>([]);

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly errorCatalogos = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  /** null = todas las reservas; un id = solo las de ese usuario (GET /api/reservas/usuario/{id}). */
  protected readonly filtroUsuario = signal<number | null>(null);

  protected nueva: ReservaRequest = formularioVacio();

  private readonly nombresUsuarios = computed(
    () => new Map(this.usuarios().map((u) => [u.id, u.nombre] as const)),
  );
  private readonly nombresLaboratorios = computed(
    () => new Map(this.laboratorios().map((l) => [l.id, l.nombre] as const)),
  );

  ngOnInit(): void {
    this.cargarCatalogos();
    this.cargarReservas();
  }

  protected nombreUsuario(id: number): string {
    return this.nombresUsuarios().get(id) ?? `Usuario ${id}`;
  }

  protected nombreLaboratorio(id: number): string {
    return this.nombresLaboratorios().get(id) ?? `Laboratorio ${id}`;
  }

  /** "08:00:00" → "08:00" */
  protected hora(valor: string): string {
    return valor ? valor.slice(0, 5) : '';
  }

  protected cambiarFiltro(valor: number | null): void {
    this.filtroUsuario.set(valor);
    this.cargarReservas();
  }

  /** Usuarios y laboratorios para los selects. Si uno de los servicios está caído, se avisa pero la página sigue funcionando. */
  protected cargarCatalogos(): void {
    this.errorCatalogos.set(null);
    forkJoin({
      usuarios: this.usuarioService.listar(),
      laboratorios: this.laboratorioService.listar(),
    }).subscribe({
      next: ({ usuarios, laboratorios }) => {
        this.usuarios.set(usuarios);
        this.laboratorios.set(laboratorios);
      },
      error: (err) => this.errorCatalogos.set('No se pudieron cargar usuarios o laboratorios: ' + mensajeDeError(err)),
    });
  }

  protected cargarReservas(): void {
    this.cargando.set(true);
    const id = this.filtroUsuario();
    const peticion = id === null ? this.reservaService.listar() : this.reservaService.porUsuario(id);
    peticion.subscribe({
      next: (lista) => {
        const ordenada = [...lista].sort((a, b) =>
          (a.fecha + a.horaInicio).localeCompare(b.fecha + b.horaInicio),
        );
        this.reservas.set(ordenada);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  protected crear(): void {
    this.error.set(null);
    this.exito.set(null);

    if (this.nueva.usuarioId === null || this.nueva.laboratorioId === null) {
      this.error.set('Seleccione un usuario y un laboratorio.');
      return;
    }

    this.guardando.set(true);
    this.reservaService.crear({ ...this.nueva }).subscribe({
      next: (creada) => {
        this.exito.set(
          `Reserva ${creada.id} creada: ${this.nombreLaboratorio(creada.laboratorioId)}, ` +
            `${creada.fecha} de ${this.hora(creada.horaInicio)} a ${this.hora(creada.horaFin)}.`,
        );
        this.guardando.set(false);
        this.cargarReservas();
      },
      error: (err) => {
        // Aquí llegan, entre otros, el fallback del Circuit Breaker (503)
        // y el límite de horas leído del ConfigMap (400).
        this.error.set(mensajeDeError(err));
        this.guardando.set(false);
      },
    });
  }

  protected eliminar(reserva: Reserva): void {
    if (reserva.id === undefined) return;
    this.error.set(null);
    this.exito.set(null);
    this.reservaService.eliminar(reserva.id).subscribe({
      next: () => {
        this.exito.set(`Reserva ${reserva.id} eliminada.`);
        this.cargarReservas();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }
}
