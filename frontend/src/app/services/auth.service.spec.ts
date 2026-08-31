import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoginRequest, UserRequest, AuthResponse } from '../models/user.model';

/**
 * AuthService Unit Tests
 * 
 * We mock:
 * - HTTP calls (HttpClientTestingModule)
 * - Router navigation
 * - localStorage (browser API)
 * 
 * What we test:
 * - Login stores token and navigates
 * - Register stores token and navigates
 * - Logout clears state and navigates
 * - Token extraction from JWT payload
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIiwiZXhwIjo5OTk5OTk5OTk5fQ.mock-signature';
  
  const mockAuthResponse: AuthResponse = {
    token: mockToken,
    type: 'Bearer',
    username: 'testuser',
    email: 'test@example.com',
    role: 'USER'
  };

  beforeEach(() => {
    // Mock Router
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    
    // Mock localStorage
    let store: { [key: string]: string } = {};
    spyOn(localStorage, 'getItem').and.callFake((key: string) => store[key] || null);
    spyOn(localStorage, 'setItem').and.callFake((key: string, value: string) => store[key] = value);
    spyOn(localStorage, 'removeItem').and.callFake((key: string) => delete store[key]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no outstanding HTTP requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should store token and navigate to dashboard on success', () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(localStorage.setItem).toHaveBeenCalledWith('taskmaster_token', mockToken);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
      });

      const req = httpMock.expectOne('/api/v1/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      req.flush(mockAuthResponse);
    });

    it('should handle login error', () => {
      const loginRequest: LoginRequest = {
        username: 'wronguser',
        password: 'wrongpass'
      };

      service.login(loginRequest).subscribe({
        error: (err) => {
          expect(err.status).toBe(401);
        }
      });

      const req = httpMock.expectOne('/api/v1/auth/login');
      req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('register', () => {
    it('should store token and navigate to dashboard on success', () => {
      const registerRequest: UserRequest = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123'
      };

      service.register(registerRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(localStorage.setItem).toHaveBeenCalledWith('taskmaster_token', mockToken);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
      });

      const req = httpMock.expectOne('/api/v1/auth/register');
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  describe('logout', () => {
    it('should clear token and navigate to login', () => {
      // First login to set state
      service['setSession'](mockAuthResponse);

      service.logout();

      expect(localStorage.removeItem).toHaveBeenCalledWith('taskmaster_token');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('getToken', () => {
    it('should return token from localStorage', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(mockToken);
      expect(service.getToken()).toBe(mockToken);
    });

    it('should return null if no token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);
      expect(service.getToken()).toBeNull();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when authenticated', () => {
      service['isAuthenticatedSubject'].next(true);
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when not authenticated', () => {
      service['isAuthenticatedSubject'].next(false);
      expect(service.isLoggedIn()).toBeFalse();
    });
  });
});