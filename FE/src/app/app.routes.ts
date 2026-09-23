import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

/**
 * ALL routes are lazy (loadComponent / loadChildren). Wildcard ALWAYS last.
 * Adapting to the exam: add one feature folder + one lazy entry here.
 */
export const routes: Routes = [
  { path: '', redirectTo: '/games', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/pages/register-page/register-page').then((m) => m.RegisterPage),
  },
  {
    path: 'games',
    canActivate: [authGuard],
    loadChildren: () => import('./features/game/game.routes').then((m) => m.GAME_ROUTES),
  },
  {
    path: '**',
    loadComponent: () => import('./core/layout/not-found-page').then((m) => m.NotFoundPage),
  },
];
