import { Routes } from '@angular/router';

export default [
  { path: '', loadComponent: () => import('./marketplace.page').then((m) => m.MarketplacePage) },
  { path: 'meus', loadComponent: () => import('./meus-anuncios.page').then((m) => m.MeusAnunciosPage) },
  { path: 'novo', loadComponent: () => import('./anuncio-form.page').then((m) => m.AnuncioFormPage) },
  { path: ':id/editar', loadComponent: () => import('./anuncio-form.page').then((m) => m.AnuncioFormPage) },
] satisfies Routes;
