import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './services/auth.service';

import { AppComponent } from './app.component';

describe('AppComponent', () => {

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        AppComponent
      ],
      providers: [
        provideRouter([]),
        provideHttpClient()
      ]
    }).compileComponents();
  });


  it('should create the application', () => {

    const fixture =
      TestBed.createComponent(AppComponent);

    const app =
      fixture.componentInstance;

    expect(app).toBeTruthy();
  });

  it('should logout and redirect to login', () => {

    const authService = TestBed.inject(AuthService);
    const router = TestBed.inject(Router);

    const logoutSpy = spyOn(authService, 'logout');
    const navigateSpy = spyOn(router, 'navigate')
      .and.returnValue(Promise.resolve(true));

    const fixture = TestBed.createComponent(AppComponent);

    fixture.componentInstance.logout();

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });


  it('should toggle and close the mobile menu', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    expect(app.mobileMenuOpen).toBeFalse();

    app.toggleMobileMenu();
    expect(app.mobileMenuOpen).toBeTrue();

    app.toggleMobileMenu();
    expect(app.mobileMenuOpen).toBeFalse();

    app.toggleMobileMenu();
    app.closeMobileMenu();

    expect(app.mobileMenuOpen).toBeFalse();
  });

});