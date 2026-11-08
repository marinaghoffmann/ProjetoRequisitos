import { Routes } from '@angular/router';
import { papelGuard } from './core/auth/papel.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'catalogo' },
  { path: 'login', loadComponent: () => import('./features/identidade/login.page').then((m) => m.LoginPage) },
  { path: 'cadastro', loadComponent: () => import('./features/identidade/cadastro.page').then((m) => m.CadastroPage) },
  {
    path: 'admin/usuarios/novo',
    canActivate: [papelGuard('ADMINISTRADOR')],
    loadComponent: () =>
      import('./features/identidade/cadastro-privilegiado.page').then((m) => m.CadastroPrivilegiadoPage),
  },
  { path: 'catalogo/novo', loadComponent: () => import('./features/catalogo/cadastro-jogo.page').then((m) => m.CadastroJogoPage) },
  { path: 'catalogo/:id/editar', loadComponent: () => import('./features/catalogo/editar-jogo.page').then((m) => m.EditarJogoPage) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo.page').then((m) => m.CatalogoPage) },
  { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') },
  { path: 'catalogo/:id', loadComponent: () => import('./features/catalogo/detalhe-jogo.page').then((m) => m.DetalheJogoPage) },
  { path: 'biblioteca/jogando-agora',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/biblioteca/jogando-agora.page').then((m) => m.JogandoAgoraPage),
  },
  { path: '**', redirectTo: 'catalogo' }
];