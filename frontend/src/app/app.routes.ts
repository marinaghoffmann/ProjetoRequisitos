import { Routes } from '@angular/router';
import { papelGuard } from './core/auth/papel.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },
  { path: 'inicio', loadComponent: () => import('./features/inicio/inicio.page').then((m) => m.InicioPage) },
  {
    path: 'perfil',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/perfil/perfil.page').then((m) => m.PerfilPage),
  },
  {
    path: 'perfil/editar',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/perfil/editar-perfil.page').then((m) => m.EditarPerfilPage),
  },
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
  { path: 'catalogo/:id', loadComponent: () => import('./features/catalogo/detalhe-jogo.page').then((m) => m.DetalheJogoPage) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo.page').then((m) => m.CatalogoPage) },
  { path: 'marketplace', loadChildren: () => import('./features/marketplace/marketplace.routes') },
  { path: 'moderacao', canActivate: [papelGuard('MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/moderacao/moderacao.page').then((m) => m.ModeracaoPage) },
  { path: 'forum', loadChildren: () => import('./features/forum/forum.routes') },
  { path: 'lancamentos', loadComponent: () => import('./features/lancamentos/lancamentos.page').then(m => m.LancamentosPage) },
  { path: 'lancamentos/novo', canActivate: [papelGuard('ADMINISTRADOR', 'DESENVOLVEDOR')],
    loadComponent: () => import('./features/lancamentos/lancamento-form.page').then(m => m.LancamentoFormPage) },
  { path: 'lancamentos/:id/editar', canActivate: [papelGuard('ADMINISTRADOR', 'DESENVOLVEDOR')],
    loadComponent: () => import('./features/lancamentos/lancamento-form.page').then(m => m.LancamentoFormPage) },
  { path: 'notificacoes', canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/notificacoes/notificacoes.page').then(m => m.NotificacoesPage) },
  { path: 'biblioteca/jogando-agora',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/biblioteca/jogando-agora.page').then((m) => m.JogandoAgoraPage),
  },
  {
    path: 'biblioteca/zerados',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/biblioteca/zerados.page').then((m) => m.ZeradosPage),
  },
  {
    path: 'timeline',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/journal/timeline.page').then((m) => m.TimelinePage),
  },
  // F-04: "Minhas listas" exige login; a página de uma lista é pública (só o dono vê os botões de edição)
  {
    path: 'biblioteca/listas',
    canActivate: [papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR')],
    loadComponent: () => import('./features/biblioteca/listas.page').then((m) => m.ListasPage),
  },
  { path: 'biblioteca/listas/:id', loadComponent: () => import('./features/biblioteca/lista.page').then((m) => m.ListaPage) },
  { path: '**', redirectTo: 'inicio' }
];
