import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { TaskListComponent } from './task-list.component';
import { TaskService } from '../../services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../models/task.model';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  const mockTasks: Task[] = [
    {
      id: 1,
      title: 'Task 1',
      description: 'Description 1',
      status: TaskStatus.TODO,
      priority: TaskPriority.HIGH,
      assignee: 'user1',
      dueDate: '2026-09-15',
      createdAt: '2026-08-29T10:00:00Z',
      updatedAt: '2026-08-29T10:00:00Z'
    },
    {
      id: 2,
      title: 'Task 2',
      description: 'Description 2',
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.MEDIUM,
      assignee: 'user2',
      dueDate: '2026-09-20',
      createdAt: '2026-08-29T10:00:00Z',
      updatedAt: '2026-08-29T10:00:00Z'
    }
  ];

  beforeEach(async () => {
    taskServiceSpy = jasmine.createSpyObj('TaskService', [
      'getAllTasks',
      'getTasksByStatus',
      'deleteTask'
    ]);
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        TaskListComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load tasks on init', fakeAsync(() => {
      taskServiceSpy.getAllTasks.and.returnValue(of(mockTasks));

      fixture.detectChanges(); // Triggers ngOnInit
      tick();

      expect(taskServiceSpy.getAllTasks).toHaveBeenCalled();
      expect(component.tasks).toEqual(mockTasks);
      expect(component.isLoading).toBeFalse();
    }));

    it('should handle error when loading tasks', fakeAsync(() => {
      taskServiceSpy.getAllTasks.and.returnValue(throwError(() => new Error('Network error')));

      fixture.detectChanges();
      tick();

      expect(component.isLoading).toBeFalse();
      expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to load tasks', 'Close', { duration: 3000 });
    }));
  });

  describe('deleteTask', () => {
    it('should remove task from list on successful delete', fakeAsync(() => {
      component.tasks = mockTasks;
      taskServiceSpy.deleteTask.and.returnValue(of(void 0));

      spyOn(window, 'confirm').and.returnValue(true);

      component.deleteTask(1);
      tick();

      expect(taskServiceSpy.deleteTask).toHaveBeenCalledWith(1);
      expect(component.tasks.length).toBe(1);
      expect(component.tasks.find(t => t.id === 1)).toBeUndefined();
      expect(snackBarSpy.open).toHaveBeenCalledWith('Task deleted', 'Close', { duration: 3000 });
    }));

    it('should not delete if user cancels', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.deleteTask(1);

      expect(taskServiceSpy.deleteTask).not.toHaveBeenCalled();
    });

    it('should show error on delete failure', fakeAsync(() => {
      component.tasks = mockTasks;
      taskServiceSpy.deleteTask.and.returnValue(throwError(() => new Error('Delete failed')));

      spyOn(window, 'confirm').and.returnValue(true);

      component.deleteTask(1);
      tick();

      expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to delete task', 'Close', { duration: 3000 });
      expect(component.tasks.length).toBe(2); // Task should still be in list
    }));
  });

  describe('filterByStatus', () => {
    it('should filter tasks by status', fakeAsync(() => {
      const todoTasks = mockTasks.filter(t => t.status === TaskStatus.TODO);
      taskServiceSpy.getTasksByStatus.and.returnValue(of(todoTasks));

      component.selectedStatus = TaskStatus.TODO;
      component.filterByStatus();
      tick();

      expect(taskServiceSpy.getTasksByStatus).toHaveBeenCalledWith(TaskStatus.TODO);
      expect(component.tasks).toEqual(todoTasks);
    }));

    it('should load all tasks when no status selected', fakeAsync(() => {
      taskServiceSpy.getAllTasks.and.returnValue(of(mockTasks));

      component.selectedStatus = '';
      component.filterByStatus();
      tick();

      expect(taskServiceSpy.getAllTasks).toHaveBeenCalled();
    }));
  });
});