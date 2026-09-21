import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

interface AuthResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl =
    `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {
  }

  register(
    email: string,
    password: string
  ): Observable<void> {

    return this.http.post<void>(
      `${this.apiUrl}/register`,
      {
        email,
        password
      }
    );
  }

  login(
    email: string,
    password: string
  ): Observable<AuthResponse> {

    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      {
        email,
        password
      }
    );
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('token') !== null;
  }

  logout(): void {
    localStorage.removeItem('token');
  }
}