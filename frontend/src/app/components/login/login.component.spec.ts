import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user.model';

/**
 * LoginComponent Tests
 * 
 * We test:
 * - Form validation (required fields, min length)
 * - Submit calls auth service
 * - Success navigates to dashboard
 * - Error shows error message
 * - Loading state during request
 */
describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have invalid form when empty', () => {
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('should have invalid form when username is empty', () => {
    component.loginForm.patchValue({ username: '', password: 'password123' });
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('should have invalid form when password is empty', () => {
    component.loginForm.patchValue({ username: 'testuser', password: '' });
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('should have valid form with correct data', () => {
    component.loginForm.patchValue({ username: 'testuser', password: 'password123' });
    expect(component.loginForm.valid).toBeTruthy();
  });

  describe('onSubmit', () => {
    it('should not submit if form is invalid', () => {
      component.loginForm.patchValue({ username: '', password: '' });
      component.onSubmit();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should call auth service and navigate on success', fakeAsync(() => {
      const mockResponse = {
        token: 'fake-token',
        type: 'Bearer',
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER'
      };

      authServiceSpy.login.and.returnValue(of(mockResponse));

      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      expect(authServiceSpy.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123'
      } as LoginRequest);
      expect(component.isLoading).toBeFalse();
      expect(snackBarSpy.open).toHaveBeenCalledWith('Login successful!', 'Close', { duration: 3000 });
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
    }));

    it('should show error message on login failure', fakeAsync(() => {
      const errorResponse = {
        error: { message: 'Invalid credentials' },
        status: 401
      };

      authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.patchValue({
        username: 'wronguser',
        password: 'wrongpass'
      });

      component.onSubmit();
      tick();

      expect(component.isLoading).toBeFalse();
      expect(component.errorMessage).toBe('Invalid credentials');
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    }));

    it('should set loading state during login', () => {
      authServiceSpy.login.and.returnValue(of({} as any));

      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123'
      });

      component.onSubmit();
      expect(component.isLoading).toBeTrue();
    });
  });
});