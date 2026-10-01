import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../environments/environment';


export const authInterceptor: HttpInterceptorFn =
  (request, next) => {

    const token =
      localStorage.getItem('token');

    const isApiRequest =
      request.url.startsWith(environment.apiUrl);

    const isPublicRequest =
      request.url.includes('/auth/login')
      || request.url.includes('/auth/register')
      || request.url.includes('/download/');


    if (
      !token
      || !isApiRequest
      || isPublicRequest
    ) {

      return next(request);
    }


    const authenticatedRequest =
      request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });


    const router = inject(Router);

    return next(authenticatedRequest).pipe(
      catchError((error: HttpErrorResponse) => {

        if (
          error.status === 401
          && localStorage.getItem('token') === token
        ) {
          localStorage.removeItem('token');

          void router.navigate(['/login']);
        }

        return throwError(() => error);
      })
    );
  };