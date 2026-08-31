import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Task, TaskStatus } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="dashboard-container">
      <h1>Welcome, {{ (authService.currentUser$ | async)?.username }}!</h1>
      
      <div class="stats-grid" *ngIf="!isLoading">
        <mat-card class="stat-card total">
          <mat-icon>assignment</mat-icon>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">Total Tasks</div>
        </mat-card>
        
        <mat-card class="stat-card todo">
          <mat-icon>schedule</mat-icon>
          <div class="stat-value">{{ stats.todo }}</div>
          <div class="stat-label">To Do</div>
        </mat-card>
        
        <mat-card class="stat-card in-progress">
          <mat-icon>autorenew</mat-icon>
          <div class="stat-value">{{ stats.inProgress }}</div>
          <div class="stat-label">In Progress</div>
        </mat-card>
        
        <mat-card class="stat-card done">
          <mat-icon>check_circle</mat-icon>
          <div class="stat-value">{{ stats.done }}</div>
          <div class="stat-label">Completed</div>
        </mat-card>
      </div>

      <div *ngIf="isLoading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <div class="actions">
        <button mat-raised-button color="primary" routerLink="/tasks/new">
          <mat-icon>add</mat-icon> Create New Task
        </button>
        <button mat-button routerLink="/tasks">
          <mat-icon>list</mat-icon> View All Tasks
        </button>
      </div>

      <div class="recent-tasks" *ngIf="!isLoading && recentTasks.length > 0">
        <h2>Recent Tasks</h2>
        <div class="task-list">
          <mat-card *ngFor="let task of recentTasks" class="recent-task-card">
            <div class="task-info">
              <span class="task-title">{{ task.title }}</span>
              <span [class]="'status-badge status-' + task.status.toLowerCase()">{{ task.status }}</span>
            </div>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    h1 { margin-bottom: 24px; font-weight: 300; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
    .stat-card { display: flex; flex-direction: column; align-items: center; padding: 24px; text-align: center; }
    .stat-card mat-icon { font-size: 40px; width: 40px; height: 40px; margin-bottom: 12px; }
    .stat-value { font-size: 36px; font-weight: 700; margin-bottom: 4px; }
    .stat-label { color: #666; font-size: 14px; text-transform: uppercase; letter-spacing: 1px; }
    .total mat-icon { color: #1976d2; }
    .todo mat-icon { color: #ff9800; }
    .in-progress mat-icon { color: #f44336; }
    .done mat-icon { color: #4caf50; }
    .loading { display: flex; justify-content: center; padding: 40px; }
    .actions { display: flex; gap: 16px; margin-bottom: 30px; }
    .recent-tasks h2 { font-weight: 400; margin-bottom: 16px; }
    .task-list { display: flex; flex-direction: column; gap: 12px; }
    .recent-task-card { padding: 16px; }
    .task-info { display: flex; justify-content: space-between; align-items: center; }
    .task-title { font-weight: 500; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500; }
    .status-todo { background: #e3f2fd; color: #1976d2; }
    .status-in_progress { background: #fff3e0; color: #f57c00; }
    .status-done { background: #e8f5e9; color: #388e3c; }
  `]
})
export class DashboardComponent implements OnInit {
  stats = { total: 0, todo: 0, inProgress: 0, done: 0 };
  recentTasks: Task[] = [];
  isLoading = true;

  constructor(
    private taskService: TaskService,
    public authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.stats.total = tasks.length;
        this.stats.todo = tasks.filter(t => t.status === TaskStatus.TODO).length;
        this.stats.inProgress = tasks.filter(t => t.status === TaskStatus.IN_PROGRESS).length;
        this.stats.done = tasks.filter(t => t.status === TaskStatus.DONE).length;
        this.recentTasks = tasks.slice(0, 5);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load dashboard', 'Close', { duration: 3000 });
      }
    });
  }
}