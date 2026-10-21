import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { PiePod } from './components/pie-pod/pie-pod';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, PiePod],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
