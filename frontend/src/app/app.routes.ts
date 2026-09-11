import { Routes } from '@angular/router';

import { RegisterComponent } from './pages/register/register.component';
import { LoginComponent } from './pages/login/login.component';
import { UploadComponent } from './pages/upload/upload.component';

export const routes: Routes = [
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'upload',
    component: UploadComponent
  }
];