import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

import {
  RouterLink,
  RouterOutlet
} from '@angular/router';


@Component({
  selector: 'app-root',

  imports: [
    RouterOutlet,
    RouterLink
  ],

  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {

  mobileMenuOpen = false;

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  logout(): void {
    this.closeMobileMenu();
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}