import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should register a user with email and password', () => {
    service
      .register('test@datashare.fr', 'Password123!')
      .subscribe();

    const request = httpMock.expectOne(
      'http://localhost:8080/api/auth/register'
    );

    expect(request.request.method).toBe('POST');

    expect(request.request.body).toEqual({
      email: 'test@datashare.fr',
      password: 'Password123!'
    });

    request.flush(null);
  });

  it('should login a user and return a JWT token', () => {
    const fakeResponse = {
      token: 'fake-jwt-token'
    };

    service
      .login('test@datashare.fr', 'Password123!')
      .subscribe(response => {
        expect(response.token).toBe('fake-jwt-token');
      });

    const request = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    expect(request.request.method).toBe('POST');

    expect(request.request.body).toEqual({
      email: 'test@datashare.fr',
      password: 'Password123!'
    });

    request.flush(fakeResponse);
  });
});