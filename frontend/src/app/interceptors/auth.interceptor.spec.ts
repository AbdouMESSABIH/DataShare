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

import {
  authInterceptor
} from './auth.interceptor';


describe('authInterceptor', () => {

  let http:
    HttpClient;

  let httpMock:
    HttpTestingController;


  beforeEach(() => {

    localStorage.clear();


    TestBed.configureTestingModule({
      providers: [

        provideHttpClient(
          withInterceptors([
            authInterceptor
          ])
        ),

        provideHttpClientTesting()

      ]
    });


    http =
      TestBed.inject(HttpClient);

    httpMock =
      TestBed.inject(HttpTestingController);
  });


  afterEach(() => {

    httpMock.verify();

    localStorage.clear();
  });


  it(
    'should add the JWT token to a protected API request',
    () => {

      localStorage.setItem(
        'token',
        'fake-jwt-token'
      );


      http
        .get(
          'http://localhost:8080/api/files'
        )
        .subscribe();


      const request =
        httpMock.expectOne(
          'http://localhost:8080/api/files'
        );


      expect(
        request.request.headers.get(
          'Authorization'
        )
      ).toBe(
        'Bearer fake-jwt-token'
      );


      request.flush([]);
    }
  );


  it(
    'should not add the JWT token to the login request',
    () => {

      localStorage.setItem(
        'token',
        'fake-jwt-token'
      );


      http
        .post(
          'http://localhost:8080/api/auth/login',
          {}
        )
        .subscribe();


      const request =
        httpMock.expectOne(
          'http://localhost:8080/api/auth/login'
        );


      expect(
        request.request.headers.has(
          'Authorization'
        )
      ).toBeFalse();


      request.flush({
        token: 'jwt'
      });
    }
  );


  it(
    'should not add the JWT token to a public download request',
    () => {

      localStorage.setItem(
        'token',
        'fake-jwt-token'
      );


      http
        .get(
          'http://localhost:8080/api/download/test-token'
        )
        .subscribe();


      const request =
        httpMock.expectOne(
          'http://localhost:8080/api/download/test-token'
        );


      expect(
        request.request.headers.has(
          'Authorization'
        )
      ).toBeFalse();


      request.flush({
        originalName: 'test.txt',
        size: 10,
        contentType: 'text/plain',
        expiresAt: '2026-09-23T12:00:00'
      });
    }
  );
});