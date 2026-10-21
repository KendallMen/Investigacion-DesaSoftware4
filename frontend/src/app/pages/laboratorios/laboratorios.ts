import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Laboratorio } from '../../models';
import { LaboratorioService } from '../../services/laboratorio.service';
import { mensajeDeError } from '../../services/mensaje-error';

const VACIO: Laboratorio = { nombre: '', edificio: '', capacidad: null, disponible: true };

@Component({
  selector: 'app-laboratorios',
  imports: [FormsModule],
  templateUrl: './laboratorios.html',
})
export class Laboratorios implements OnInit {
  private readonly service = inject(LaboratorioService);

  protected readonly laboratorios = signal<Laboratorio[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected nuevo: Laboratorio = { ...VACIO };

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.service.listar().subscribe({
      next: (lista) => {
        this.laboratorios.set(lista);
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
    this.guardando.set(true);
    this.service.crear({ ...this.nuevo }).subscribe({
      next: (creado) => {
        this.exito.set(`Laboratorio ${creado.nombre} creado con id ${creado.id}.`);
        this.nuevo = { ...VACIO };
        this.guardando.set(false);
        this.cargar();
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.guardando.set(false);
      },
    });
  }
}
