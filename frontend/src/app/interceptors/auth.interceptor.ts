import {
  HttpInterceptorFn
} from '@angular/common/http';

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


    return next(authenticatedRequest);
  };