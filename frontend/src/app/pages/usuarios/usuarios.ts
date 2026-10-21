import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Usuario } from '../../models';
import { mensajeDeError } from '../../services/mensaje-error';
import { UsuarioService } from '../../services/usuario.service';

const VACIO: Usuario = { nombre: '', carnet: '', correo: '', carrera: '' };

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule],
  templateUrl: './usuarios.html',
})
export class Usuarios implements OnInit {
  private readonly service = inject(UsuarioService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected nuevo: Usuario = { ...VACIO };

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.service.listar().subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
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
        this.exito.set(`Usuario ${creado.nombre} creado con id ${creado.id}.`);
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
