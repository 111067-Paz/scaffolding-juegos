import { Routes } from '@angular/router';

/** Feature routes: every page lazy-loaded. The parent route applies authGuard. */
export const GAME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/game-list-page/game-list-page').then((m) => m.GameListPage),
  },
  {
    path: 'new',
    loadComponent: () => import('./pages/game-new-page/game-new-page').then((m) => m.GameNewPage),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/game-board-page/game-board-page').then((m) => m.GameBoardPage),
  },
];
