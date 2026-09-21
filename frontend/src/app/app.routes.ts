import { Routes } from '@angular/router';

import { RegisterComponent } from './pages/register/register.component';
import { LoginComponent } from './pages/login/login.component';
import { UploadComponent } from './pages/upload/upload.component';
import { DownloadComponent } from './pages/download/download.component';
import { HistoryComponent } from './pages/history/history.component';

import { authGuard } from './guards/auth.guard';


export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

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
    component: UploadComponent,
    canActivate: [authGuard]
  },

  {
    path: 'history',
    component: HistoryComponent,
    canActivate: [authGuard]
  },

  {
    path: 'download/:token',
    component: DownloadComponent
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];