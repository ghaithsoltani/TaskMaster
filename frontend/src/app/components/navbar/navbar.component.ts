import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NgIf, AsyncPipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, NgIf, AsyncPipe],
  template: `
    <mat-toolbar color="primary" class="navbar">
      <span class="brand" routerLink="/dashboard">
        <mat-icon>assignment</mat-icon>
        TaskMaster
      </span>
      
      <span class="spacer"></span>
      
      <ng-container *ngIf="authService.isAuthenticated$ | async; else guest">
        <button mat-button routerLink="/dashboard">
          <mat-icon>dashboard</mat-icon> Dashboard
        </button>
        <button mat-button (click)="logout()">
          <mat-icon>logout</mat-icon> Logout
        </button>
      </ng-container>
      
      <ng-template #guest>
        <button mat-button routerLink="/login">Login</button>
        <button mat-button routerLink="/register">Register</button>
      </ng-template>
    </mat-toolbar>
  `,
  styles: [`
    .navbar { position: sticky; top: 0; z-index: 1000; }
    .brand { display: flex; align-items: center; gap: 8px; cursor: pointer; font-weight: 600; }
    .spacer { flex: 1 1 auto; }
    mat-icon { vertical-align: middle; }
  `]
})
export class NavbarComponent {
  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
  }
}