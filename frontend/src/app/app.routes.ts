import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'reservas' },
  {
    path: 'reservas',
    title: 'Reservas',
    loadComponent: () => import('./pages/reservas/reservas').then((m) => m.Reservas),
  },
  {
    path: 'usuarios',
    title: 'Usuarios',
    loadComponent: () => import('./pages/usuarios/usuarios').then((m) => m.Usuarios),
  },
  {
    path: 'laboratorios',
    title: 'Laboratorios',
    loadComponent: () => import('./pages/laboratorios/laboratorios').then((m) => m.Laboratorios),
  },
  { path: '**', redirectTo: 'reservas' },
];
