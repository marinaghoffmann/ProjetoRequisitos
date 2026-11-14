import { Routes } from '@angular/router';

import { papelGuard } from '../../core/auth/papel.guard';

const logado = papelGuard('COMUM', 'DESENVOLVEDOR', 'MODERADOR', 'ADMINISTRADOR');

export default [
  { path: '', loadComponent: () => import('./marketplace.page').then((m) => m.MarketplacePage) },
  { path: 'meus', loadComponent: () => import('./meus-anuncios.page').then((m) => m.MeusAnunciosPage) },
  { path: 'novo', loadComponent: () => import('./anuncio-form.page').then((m) => m.AnuncioFormPage) },
  { path: 'vendedores/:id', loadComponent: () => import('./perfil-vendedor.page').then((m) => m.PerfilVendedorPage) },
  {
    path: 'mensagens',
    canActivate: [logado],
    loadComponent: () => import('./mensagens.page').then((m) => m.MensagensPage),
  },
  {
    path: 'mensagens/:id',
    canActivate: [logado],
    loadComponent: () => import('./conversa.page').then((m) => m.ConversaPage),
  },
  {
    path: 'checkout',
    canActivate: [logado],
    loadComponent: () => import('./checkout.page').then((m) => m.CheckoutPage),
  },
  {
    path: 'pedidos',
    canActivate: [logado],
    loadComponent: () => import('./pedidos.page').then((m) => m.PedidosPage),
  },
  { path: ':id/editar', loadComponent: () => import('./anuncio-form.page').then((m) => m.AnuncioFormPage) },
  { path: ':id', loadComponent: () => import('./anuncio.page').then((m) => m.AnuncioPage) },
] satisfies Routes;
