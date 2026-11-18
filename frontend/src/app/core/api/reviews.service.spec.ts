import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Review, ReviewsService } from './reviews.service';

describe('ReviewsService', () => {
  let servico: ReviewsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servico = TestBed.inject(ReviewsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('devolve null quando o usuário ainda não avaliou o jogo (204)', () => {
    let resultado: Review | null | undefined;
    servico.minhaDoJogo('jogo-1').subscribe((review) => (resultado = review));

    http.expectOne('/api/avaliacao/jogos/jogo-1/reviews/minha').flush(null, { status: 204, statusText: 'No Content' });

    expect(resultado).toBeNull();
  });

  it('consulta as reviews do jogo com a página e o tamanho', () => {
    servico.doJogo('jogo-1', 2, 5).subscribe();

    const requisicao = http.expectOne((r) => r.url === '/api/avaliacao/jogos/jogo-1/reviews');
    expect(requisicao.request.params.get('pagina')).toBe('2');
    expect(requisicao.request.params.get('tamanho')).toBe('5');
    requisicao.flush({});
  });
});
