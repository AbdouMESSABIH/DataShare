import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  AuthService
} from '../../services/auth.service';


@Component({
  selector: 'app-register',

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './register.component.html',

  styleUrl:
    './register.component.scss'
})
export class RegisterComponent {

  email = '';

  password = '';

  passwordConfirmation = '';

  message = '';

  errorMessage = '';


  constructor(
    private authService: AuthService
  ) {
  }


  onSubmit(): void {

    this.message = '';

    this.errorMessage = '';


    if (
      this.password
      !== this.passwordConfirmation
    ) {

      this.errorMessage =
        'Les mots de passe ne correspondent pas';

      return;
    }


    this.authService
      .register(
        this.email,
        this.password
      )
      .subscribe({

        next: () => {

          this.message =
            'Compte créé avec succès';

          this.email = '';

          this.password = '';

          this.passwordConfirmation = '';
        },


        error: (error) => {

          if (
            error.status === 409
          ) {

            this.errorMessage =
              'Cet email est déjà utilisé';

          } else if (
            error.status === 400
          ) {

            this.errorMessage =
              'Les informations saisies sont invalides';

          } else {

            this.errorMessage =
              'Une erreur est survenue';
          }
        }
      });
  }
}