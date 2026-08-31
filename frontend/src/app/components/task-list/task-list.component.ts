import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { Task, TaskStatus, TaskPriority } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule
  ],
  template: `
    <div class="task-list-container">
      <div class="header">
        <h2>My Tasks</h2>
        <button mat-raised-button color="primary" routerLink="/tasks/new">
          <mat-icon>add</mat-icon> New Task
        </button>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline">
          <mat-label>Filter by Status</mat-label>
          <mat-select [(ngModel)]="selectedStatus" (selectionChange)="filterByStatus()">
            <mat-option value="">All</mat-option>
            <mat-option *ngFor="let status of statuses" [value]="status">{{ status }}</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div *ngIf="isLoading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <div *ngIf="!isLoading && tasks.length === 0" class="empty-state">
        <mat-icon>inbox</mat-icon>
        <p>No tasks yet. Create your first task!</p>
      </div>

      <div class="task-grid" *ngIf="!isLoading">
        <mat-card *ngFor="let task of tasks" class="task-card" [class]="'priority-' + task.priority.toLowerCase()">
          <mat-card-header>
            <mat-card-title>{{ task.title }}</mat-card-title>
            <mat-card-subtitle>
              <span [class]="'status-badge status-' + task.status.toLowerCase()">{{ task.status }}</span>
            </mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <p *ngIf="task.description">{{ task.description }}</p>
            <div class="task-meta">
              <span><mat-icon>person</mat-icon> {{ task.assignee }}</span>
              <span *ngIf="task.dueDate"><mat-icon>event</mat-icon> {{ task.dueDate | date }}</span>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button color="primary" [routerLink]="['/tasks/edit', task.id]">
              <mat-icon>edit</mat-icon> Edit
            </button>
            <button mat-button color="warn" (click)="deleteTask(task.id)">
              <mat-icon>delete</mat-icon> Delete
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .task-list-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .filters { margin-bottom: 20px; }
    .loading { display: flex; justify-content: center; padding: 40px; }
    .empty-state { text-align: center; padding: 60px; color: #666; }
    .empty-state mat-icon { font-size: 64px; width: 64px; height: 64px; color: #ccc; }
    .task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px; }
    .task-card { transition: transform 0.2s, box-shadow 0.2s; }
    .task-card:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }
    .priority-low { border-left: 4px solid #4caf50; }
    .priority-medium { border-left: 4px solid #ff9800; }
    .priority-high { border-left: 4px solid #f44336; }
    .priority-urgent { border-left: 4px solid #9c27b0; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500; }
    .status-todo { background: #e3f2fd; color: #1976d2; }
    .status-in_progress { background: #fff3e0; color: #f57c00; }
    .status-done { background: #e8f5e9; color: #388e3c; }
    .task-meta { display: flex; gap: 16px; margin-top: 12px; font-size: 14px; color: #666; }
    .task-meta mat-icon { font-size: 16px; width: 16px; height: 16px; vertical-align: middle; margin-right: 4px; }
    mat-card-actions { display: flex; justify-content: flex-end; gap: 8px; }
  `]
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  isLoading = true;
  selectedStatus = '';
  statuses = Object.values(TaskStatus);

  constructor(
    private taskService: TaskService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.isLoading = true;
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load tasks', 'Close', { duration: 3000 });
      }
    });
  }

  filterByStatus(): void {
    if (!this.selectedStatus) {
      this.loadTasks();
      return;
    }
    this.isLoading = true;
    this.taskService.getTasksByStatus(this.selectedStatus as TaskStatus).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to filter tasks', 'Close', { duration: 3000 });
      }
    });
  }

  deleteTask(id: number): void {
    if (!confirm('Are you sure you want to delete this task?')) return;
    
    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(t => t.id !== id);
        this.snackBar.open('Task deleted', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to delete task', 'Close', { duration: 3000 });
      }
    });
  }
}