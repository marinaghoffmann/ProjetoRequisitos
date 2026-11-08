import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AutenticacaoService, Papel } from './autenticacao.service';

export const papelGuard = (...papeis: Papel[]): CanActivateFn => () => {
  const autenticacao = inject(AutenticacaoService);
  return autenticacao.temPapel(...papeis) || inject(Router).createUrlTree(['/login']);
};