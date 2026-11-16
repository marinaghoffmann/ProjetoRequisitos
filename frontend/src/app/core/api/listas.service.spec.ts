import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ListasService } from './listas.service';

describe('ListasService', () => {
  let servico: ListasService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servico = TestBed.inject(ListasService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('adiciona o jogo com a nota explicativa', () => {
    servico.adicionarJogo('lista-1', 'jogo-1', 'Trilha sonora perfeita').subscribe();

    const requisicao = http.expectOne('/api/biblioteca/listas/lista-1/itens');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({ jogoId: 'jogo-1', notaExplicativa: 'Trilha sonora perfeita' });
    requisicao.flush({});
  });

  it('edita a nota pelo jogo do item', () => {
    servico.editarNota('lista-1', 'jogo-1', 'Nova nota').subscribe();

    const requisicao = http.expectOne('/api/biblioteca/listas/lista-1/itens/jogo-1');
    expect(requisicao.request.method).toBe('PATCH');
    requisicao.flush({});
  });
});
