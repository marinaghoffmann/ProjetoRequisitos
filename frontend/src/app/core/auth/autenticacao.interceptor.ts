import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AutenticacaoService } from './autenticacao.service';

/**
 * Acrescenta a credencial Bearer às chamadas da API; a autorização continua no backend.
 * Se o backend recusar o token (expirado ou inválido), encerra a sessão local para que as
 * próximas chamadas, inclusive as públicas, não continuem enviando um token rejeitado.
 */
export const autenticacaoInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const autenticacao = inject(AutenticacaoService);
  const token = autenticacao.tokenAtual();
  if (!token || !requisicao.url.startsWith('/api/')) return proximo(requisicao);

  return proximo(requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401) autenticacao.sair();
      return throwError(() => erro);
    }),
  );
};
