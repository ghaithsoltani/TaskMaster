import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [authGuard] },
  { path: 'tasks', loadComponent: () => import('./components/task-list/task-list.component').then(m => m.TaskListComponent), canActivate: [authGuard] },
  { path: 'tasks/new', loadComponent: () => import('./components/task-form/task-form.component').then(m => m.TaskFormComponent), canActivate: [authGuard] },
  { path: 'tasks/edit/:id', loadComponent: () => import('./components/task-form/task-form.component').then(m => m.TaskFormComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: '/dashboard' }
];