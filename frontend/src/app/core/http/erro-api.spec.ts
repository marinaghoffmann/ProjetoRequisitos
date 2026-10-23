import { HttpErrorResponse } from '@angular/common/http';

import { paraErroApi } from './erro-api';

describe('paraErroApi', () => {
  it('usa o detail do Problem Details como mensagem', () => {
    const erro = new HttpErrorResponse({
      status: 422,
      error: { title: 'Regra de negócio violada', detail: 'Só é possível negociar anúncios ativos' },
    });
    expect(paraErroApi(erro)).toEqual({
      status: 422,
      titulo: 'Regra de negócio violada',
      mensagem: 'Só é possível negociar anúncios ativos',
    });
  });

  it('trata falta de conexão', () => {
    expect(paraErroApi(new HttpErrorResponse({ status: 0 })).titulo).toBe('Sem conexão');
  });
});
