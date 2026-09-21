import {
  of,
  throwError
} from 'rxjs';

import { Router } from '@angular/router';

import {
  LoginComponent
} from './login.component';

import {
  AuthService
} from '../../services/auth.service';


describe('LoginComponent', () => {

  let component: LoginComponent;

  let authService:
    jasmine.SpyObj<AuthService>;

  let router:
    jasmine.SpyObj<Router>;


  beforeEach(() => {

    localStorage.clear();


    authService =
      jasmine.createSpyObj<AuthService>(
        'AuthService',
        ['login']
      );


    router =
      jasmine.createSpyObj<Router>(
        'Router',
        ['navigate']
      );


    router.navigate.and.returnValue(
      Promise.resolve(true)
    );


    component =
      new LoginComponent(
        authService,
        router
      );
  });


  afterEach(() => {

    localStorage.clear();
  });


  it(
    'should store the JWT token and redirect after a successful login',
    () => {

      authService.login.and.returnValue(
        of({
          token: 'fake-jwt-token'
        })
      );


      component.email =
        'test@datashare.fr';

      component.password =
        'Password123!';


      component.onSubmit();


      expect(
        authService.login
      ).toHaveBeenCalledWith(
        'test@datashare.fr',
        'Password123!'
      );


      expect(
        localStorage.getItem('token')
      ).toBe('fake-jwt-token');


      expect(
        component.message
      ).toBe('Connexion réussie');


      expect(
        component.password
      ).toBe('');


      expect(
        router.navigate
      ).toHaveBeenCalledWith([
        '/upload'
      ]);
    }
  );


  it(
    'should display a specific message for HTTP 401',
    () => {

      authService.login.and.returnValue(
        throwError(() => ({
          status: 401
        }))
      );


      component.onSubmit();


      expect(
        component.errorMessage
      ).toBe(
        'Email ou mot de passe incorrect'
      );
    }
  );


  it(
    'should display a specific message for HTTP 400',
    () => {

      authService.login.and.returnValue(
        throwError(() => ({
          status: 400
        }))
      );


      component.onSubmit();


      expect(
        component.errorMessage
      ).toBe(
        'Les informations saisies sont invalides'
      );
    }
  );


  it(
    'should display a generic message for an unexpected error',
    () => {

      authService.login.and.returnValue(
        throwError(() => ({
          status: 500
        }))
      );


      component.onSubmit();


      expect(
        component.errorMessage
      ).toBe(
        'Une erreur est survenue'
      );
    }
  );
});