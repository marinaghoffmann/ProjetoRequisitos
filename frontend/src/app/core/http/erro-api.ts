import { HttpErrorResponse } from '@angular/common/http';

/**
 * Erro devolvido pelo backend no formato Problem Details (RFC 9457).
 * "mensagem" é o texto da regra de negócio violada, pronto para mostrar ao usuário.
 */
export interface ErroApi {
  status: number;
  titulo: string;
  mensagem: string;
}

export function paraErroApi(erro: HttpErrorResponse): ErroApi {
  const corpo = erro.error ?? {};
  if (erro.status === 0) {
    return { status: 0, titulo: 'Sem conexão', mensagem: 'Não foi possível falar com o servidor.' };
  }
  return {
    status: erro.status,
    titulo: corpo.title ?? 'Erro',
    mensagem: corpo.detail ?? `Ocorreu um erro inesperado (HTTP ${erro.status}).`,
  };
}
