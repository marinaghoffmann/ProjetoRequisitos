import { Routes } from '@angular/router';

/**
 * Cada contexto do domínio ganha sua pasta em features/ e suas rotas carregadas sob demanda:
 *   { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') }
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'catalogo' },
  { path: 'login', loadComponent: () => import('./features/identidade/login.page').then((m) => m.LoginPage) },
  { path: 'cadastro', loadComponent: () => import('./features/identidade/cadastro.page').then((m) => m.CadastroPage) },
  { path: 'catalogo/novo', loadComponent: () => import('./features/catalogo/cadastro-jogo.page').then((m) => m.CadastroJogoPage) },
  { path: 'catalogo/:id/editar', loadComponent: () => import('./features/catalogo/editar-jogo.page').then((m) => m.EditarJogoPage) },
  { path: 'catalogo/:id', loadComponent: () => import('./features/catalogo/jogo.page').then((m) => m.JogoPage) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo.page').then((m) => m.CatalogoPage) },
  { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') },
  { path: 'forum', loadChildren: () => import('./features/forum/forum.routes') },
  { path: '**', redirectTo: 'catalogo' },
];
