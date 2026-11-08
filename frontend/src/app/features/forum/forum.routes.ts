import { Routes } from '@angular/router';

export default [
  { path: '', loadComponent: () => import('./forum.page').then((m) => m.ForumPage) },
  { path: 'novo', loadComponent: () => import('./topico-form.page').then((m) => m.TopicoFormPage) },
  { path: ':id', loadComponent: () => import('./topico.page').then((m) => m.TopicoPage) },
  { path: ':id/editar', loadComponent: () => import('./topico-form.page').then((m) => m.TopicoFormPage) },
] satisfies Routes;
