import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home/home').then(m => m.Home)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register/register').then(m => m.Register)
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin/admin').then(m => m.Admin),
    canActivate: [authGuard],
    data: { role: 'admin' }
  },
  {
    path: 'corporate',
    loadComponent: () => import('./pages/corporate/corporate/corporate').then(m => m.Corporate),
    canActivate: [authGuard],
    data: { role: 'corporate' }
  },
  {
    path: 'individual',
    loadComponent: () => import('./pages/individual/individual/individual').then(m => m.IndividualComponent),
    canActivate: [authGuard],
    data: { role: 'individual' }
  }
];