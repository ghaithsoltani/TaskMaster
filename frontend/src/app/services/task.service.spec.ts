import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';
import { Task, TaskRequest, TaskStatus, TaskPriority } from '../models/task.model';

/**
 * TaskService Unit Tests
 * 
 * Tests all CRUD operations and verifies:
 * - Correct HTTP methods (GET, POST, PUT, DELETE)
 * - Correct URLs
 * - Request/response handling
 */
describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const mockTask: Task = {
    id: 1,
    title: 'Test Task',
    description: 'Test Description',
    status: TaskStatus.TODO,
    priority: TaskPriority.HIGH,
    assignee: 'testuser',
    dueDate: '2026-09-15',
    createdAt: '2026-08-29T10:00:00Z',
    updatedAt: '2026-08-29T10:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService]
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllTasks', () => {
    it('should return array of tasks', () => {
      const mockTasks = [mockTask];

      service.getAllTasks().subscribe(tasks => {
        expect(tasks).toEqual(mockTasks);
        expect(tasks.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/v1/tasks');
      expect(req.request.method).toBe('GET');
      req.flush(mockTasks);
    });

    it('should handle empty task list', () => {
      service.getAllTasks().subscribe(tasks => {
        expect(tasks).toEqual([]);
        expect(tasks.length).toBe(0);
      });

      const req = httpMock.expectOne('/api/v1/tasks');
      req.flush([]);
    });
  });

  describe('getTaskById', () => {
    it('should return single task', () => {
      service.getTaskById(1).subscribe(task => {
        expect(task).toEqual(mockTask);
        expect(task.id).toBe(1);
      });

      const req = httpMock.expectOne('/api/v1/tasks/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockTask);
    });
  });

  describe('createTask', () => {
    it('should create and return task', () => {
      // Ensure this also has description:
      const newTask: TaskRequest = {
        title: 'New Task',
        description: 'New Description',  // <-- Ensure this exists
        priority: TaskPriority.MEDIUM
      };

      service.createTask(newTask).subscribe(task => {
        expect(task.id).toBeDefined();
        expect(task.title).toBe(newTask.title);
      });

      const req = httpMock.expectOne('/api/v1/tasks');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newTask);
      req.flush({ ...mockTask, ...newTask });
    });
  });

  describe('updateTask', () => {
    it('should update and return task', () => {
      // AFTER (fixed):
      const updateRequest: TaskRequest = {
        title: 'Updated Task',
        description: 'Updated description',  // <-- ADD THIS
        priority: TaskPriority.LOW,
        status: TaskStatus.IN_PROGRESS
      };

      service.updateTask(1, updateRequest).subscribe(task => {
        expect(task.title).toBe('Updated Task');
      });

      const req = httpMock.expectOne('/api/v1/tasks/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush({ ...mockTask, ...updateRequest });
    });
  });

  describe('deleteTask', () => {
    it('should delete task', () => {
      service.deleteTask(1).subscribe(() => {
        // Void response - just verify completion
        expect(true).toBeTrue();
      });

      const req = httpMock.expectOne('/api/v1/tasks/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('searchTasks', () => {
    it('should search with query parameter', () => {
      service.searchTasks('test').subscribe(tasks => {
        expect(tasks).toEqual([mockTask]);
      });

      const req = httpMock.expectOne('/api/v1/tasks/search?q=test');
      expect(req.request.method).toBe('GET');
      req.flush([mockTask]);
    });
  });

  describe('getTasksByStatus', () => {
    it('should filter by status', () => {
      service.getTasksByStatus(TaskStatus.DONE).subscribe(tasks => {
        expect(tasks).toEqual([mockTask]);
      });

      const req = httpMock.expectOne('/api/v1/tasks/status/DONE');
      expect(req.request.method).toBe('GET');
      req.flush([mockTask]);
    });
  });
});