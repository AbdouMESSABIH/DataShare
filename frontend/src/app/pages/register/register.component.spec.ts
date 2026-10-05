import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {

  let component: RegisterComponent;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>(
      'AuthService',
      ['register']
    );

    component = new RegisterComponent(authService);
  });

  it('should reject different passwords', () => {
    component.email = 'test@example.com';
    component.password = 'Password123';
    component.passwordConfirmation = 'Different123';

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Les mots de passe ne correspondent pas');

    expect(authService.register)
      .not.toHaveBeenCalled();
  });

  it('should register successfully and reset the form', () => {
    component.email = 'test@example.com';
    component.password = 'Password123';
    component.passwordConfirmation = 'Password123';

    authService.register.and.returnValue(
      of(void 0)
    );

    component.onSubmit();

    expect(authService.register)
      .toHaveBeenCalledWith(
        'test@example.com',
        'Password123'
      );

    expect(component.message)
      .toBe('Compte créé avec succès');

    expect(component.email).toBe('');
    expect(component.password).toBe('');
    expect(component.passwordConfirmation).toBe('');
  });

  it('should display an error when email already exists', () => {
    component.email = 'test@example.com';
    component.password = 'Password123';
    component.passwordConfirmation = 'Password123';

    authService.register.and.returnValue(
      throwError(() => ({ status: 409 }))
    );

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Cet email est déjà utilisé');
  });

  it('should display an error for invalid data', () => {
    component.email = 'invalid';
    component.password = 'Password123';
    component.passwordConfirmation = 'Password123';

    authService.register.and.returnValue(
      throwError(() => ({ status: 400 }))
    );

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Les informations saisies sont invalides');
  });

  it('should display a generic error', () => {
    component.email = 'test@example.com';
    component.password = 'Password123';
    component.passwordConfirmation = 'Password123';

    authService.register.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Une erreur est survenue');
  });

});
