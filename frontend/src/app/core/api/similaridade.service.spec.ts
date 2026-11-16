import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SimilaridadeService } from './similaridade.service';

describe('SimilaridadeService', () => {
  let servico: SimilaridadeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servico = TestBed.inject(SimilaridadeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta os similares com limite e nota mínima', () => {
    servico.similares('jogo-1', 5, 4).subscribe();

    const requisicao = http.expectOne((r) => r.url === '/api/descoberta/jogos/jogo-1/similares');
    expect(requisicao.request.params.get('limite')).toBe('5');
    expect(requisicao.request.params.get('notaMinima')).toBe('4');
    requisicao.flush([]);
  });

  it('sem nota mínima, não envia o filtro', () => {
    servico.similares('jogo-1').subscribe();

    const requisicao = http.expectOne((r) => r.url === '/api/descoberta/jogos/jogo-1/similares');
    expect(requisicao.request.params.has('notaMinima')).toBe(false);
    requisicao.flush([]);
  });

  it('troca o feedback com PUT no recurso da sugestão', () => {
    servico.editarFeedback('sugestao-1', 'NAO_RELEVANTE').subscribe();

    const requisicao = http.expectOne('/api/descoberta/similaridades/sugestao-1/feedback');
    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual({ tipo: 'NAO_RELEVANTE' });
    requisicao.flush({});
  });
});
