import { TestBed } from '@angular/core/testing';

import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';

import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { Router } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { environment } from '../../environments/environment';

describe('AuthInterceptor', () => {

  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {

    localStorage.removeItem('token');

    router = jasmine.createSpyObj<Router>(
      'Router',
      ['navigate']
    );

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([authInterceptor])
        ),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: router
        }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('token');
  });

  it('should clear the expired JWT and redirect after HTTP 401', () => {

    localStorage.setItem('token', 'expired-token');

    const http = TestBed.inject(
      HttpClient
    );

    http.get(`${environment.apiUrl}/files`)
      .subscribe({
        error: () => undefined
      });

    const request = httpMock.expectOne(
      `${environment.apiUrl}/files`
    );

    expect(
      request.request.headers.get('Authorization')
    ).toBe('Bearer expired-token');

    request.flush(
      { error: 'Unauthorized' },
      {
        status: 401,
        statusText: 'Unauthorized'
      }
    );

    expect(localStorage.getItem('token')).toBeNull();

    expect(router.navigate)
      .toHaveBeenCalledWith(['/login']);
  });

  it('should not clear the session on a public HTTP 401', () => {

    localStorage.setItem('token', 'existing-token');

    const http = TestBed.inject(
      HttpClient
    );

    http.post(`${environment.apiUrl}/auth/login`, {})
      .subscribe({
        error: () => undefined
      });

    const request = httpMock.expectOne(
      `${environment.apiUrl}/auth/login`
    );

    expect(
      request.request.headers.has('Authorization')
    ).toBeFalse();

    request.flush(
      { error: 'Invalid credentials' },
      {
        status: 401,
        statusText: 'Unauthorized'
      }
    );

    expect(localStorage.getItem('token'))
      .toBe('existing-token');

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should add the JWT token to a protected API request', () => {
    localStorage.setItem('token', 'fake-jwt-token');

    const http = TestBed.inject(HttpClient);

    http.get(`${environment.apiUrl}/files`).subscribe();

    const request = httpMock.expectOne(
      `${environment.apiUrl}/files`
    );

    expect(request.request.headers.get('Authorization'))
      .toBe('Bearer fake-jwt-token');

    request.flush([]);
  });

  it('should not add the JWT token to the login request', () => {
    localStorage.setItem('token', 'fake-jwt-token');

    const http = TestBed.inject(HttpClient);

    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();

    const request = httpMock.expectOne(
      `${environment.apiUrl}/auth/login`
    );

    expect(request.request.headers.has('Authorization'))
      .toBeFalse();

    request.flush({ token: 'jwt' });
  });

  it('should not add the JWT token to a public download request', () => {
    localStorage.setItem('token', 'fake-jwt-token');

    const http = TestBed.inject(HttpClient);

    http.get(`${environment.apiUrl}/download/test-token`).subscribe();

    const request = httpMock.expectOne(
      `${environment.apiUrl}/download/test-token`
    );

    expect(request.request.headers.has('Authorization'))
      .toBeFalse();

    request.flush({
      originalName: 'test.txt',
      size: 10,
      contentType: 'text/plain',
      expiresAt: '2026-09-23T12:00:00',
      passwordProtected: false
    });
  });

});
