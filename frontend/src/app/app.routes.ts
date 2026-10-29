import { Routes } from '@angular/router';

/**
 * Cada contexto do domínio ganha sua pasta em features/ e suas rotas carregadas sob demanda:
 *   { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') }
 */
export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/inicio/inicio.page').then((m) => m.InicioPage) },
  { path: 'login', loadComponent: () => import('./features/identidade/login.page').then((m) => m.LoginPage) },
  { path: 'cadastro', loadComponent: () => import('./features/identidade/cadastro.page').then((m) => m.CadastroPage) },
  { path: 'catalogo/novo', loadComponent: () => import('./features/catalogo/cadastro-jogo.page').then((m) => m.CadastroJogoPage) },
  { path: 'catalogo/:id/editar', loadComponent: () => import('./features/catalogo/editar-jogo.page').then((m) => m.EditarJogoPage) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo.page').then((m) => m.CatalogoPage) },
  { path: '**', redirectTo: '' },
];
