import { Routes } from '@angular/router';

/**
 * Cada contexto do domínio ganha sua pasta em features/ e suas rotas carregadas sob demanda:
 *   { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') }
 */
export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/inicio/inicio.page').then((m) => m.InicioPage) },
  { path: '**', redirectTo: '' },
];
