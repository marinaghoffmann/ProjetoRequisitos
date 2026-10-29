import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AutenticacaoService } from './autenticacao.service';

/** Acrescenta a credencial Bearer às chamadas da API; a autorização continua no backend. */
export const autenticacaoInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const token = inject(AutenticacaoService).tokenAtual();
  if (!token || !requisicao.url.startsWith('/api/')) return proximo(requisicao);

  return proximo(requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
