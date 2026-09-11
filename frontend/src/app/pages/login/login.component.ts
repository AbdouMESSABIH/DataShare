import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  email = '';
  password = '';

  message = '';
  errorMessage = '';

  constructor(private authService: AuthService) {
  }

  onSubmit(): void {

    this.message = '';
    this.errorMessage = '';

    this.authService
      .login(this.email, this.password)
      .subscribe({

        next: (response) => {

          localStorage.setItem(
            'token',
            response.token
          );

          this.message = 'Connexion réussie';

          this.password = '';
        },

        error: (error) => {

          if (error.status === 401) {
            this.errorMessage =
              'Email ou mot de passe incorrect';
          } else if (error.status === 400) {
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