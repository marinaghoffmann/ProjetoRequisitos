import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { AutenticacaoService } from '../core/auth/autenticacao.service';
import { BuscaPage } from './busca/busca.page';
import { DescobrirPage } from './descobrir/descobrir.page';
import { EstatisticasPage } from './estatisticas/estatisticas.page';
import { WishlistPage } from './biblioteca/wishlist.page';

registerLocaleData(localePt, 'pt-BR');

const jogo = (id: string, titulo: string, generosIds: string[], data: string) => ({
  id, titulo, tituloOriginal: '', slug: id, resumo: '', desenvolvedoraId: 'd1', generosIds, dataLancamento: data,
  capaUrl: '', trailerUrl: '', metadadosJson: '{"plataformas":["PC"]}', origem: 'API', status: 'ATIVO',
});
const ficha = (jogoId: string, publica: boolean, media: number | null, reviews: number) => ({
  jogoId, titulo: jogoId, capaUrl: null, generos: ['RPG'], publica, totalReviewsAtivas: reviews, minimoDeReviews: 3,
  mediaGeral: media, totalConclusoes: publica ? 2 : null, totalEmWishlists: publica ? 1 : null,
  totalNegociacoes: publica ? 0 : null, atualizadaEm: null,
});

function configurar(logado: boolean) {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), { provide: LOCALE_ID, useValue: 'pt-BR' },
      { provide: AutenticacaoService, useValue: {
        usuarioAtual: () => (logado ? { id: 'u1', nome: 'Marina', papel: 'COMUM' } : null),
        temPapel: () => false,
      } }],
  });
  return TestBed.inject(HttpTestingController);
}

/** Responde à carga da vitrine: catálogo, gêneros, estatísticas e (logado) wishlist. */
function responderVitrine(http: HttpTestingController, logado: boolean) {
  http.expectOne((r) => r.url === '/api/catalogo/jogos').flush([
    jogo('a', 'Alpha', ['g1'], '2020-01-01'), jogo('b', 'Beta', ['g2'], '2023-01-01'),
  ]);
  http.expectOne('/api/catalogo/generos').flush([{ id: 'g1', nome: 'Ação' }, { id: 'g2', nome: 'RPG' }]);
  http.expectOne((r) => r.url === '/api/estatisticas').flush([ficha('a', false, null, 1), ficha('b', true, 4.5, 3)]);
  if (logado) http.expectOne('/api/perfis/me/wishlist').flush([]);
}

describe('Descobrir', () => {
  it('ordena pela nota da comunidade e filtra pelo chip de gênero na hora', () => {
    const http = configurar(false);
    const f = TestBed.createComponent(DescobrirPage);
    f.detectChanges();
    responderVitrine(http, false);
    f.detectChanges();
    const titulos = () => [...f.nativeElement.querySelectorAll('.poster__titulo')].map((e: Element) => e.textContent);
    expect(titulos()).toEqual(['Beta', 'Alpha']);
    const acao = [...f.nativeElement.querySelectorAll('.chip')].find((b: Element) => b.textContent?.trim() === 'Ação') as HTMLButtonElement;
    acao.click();
    f.detectChanges();
    expect(titulos()).toEqual(['Alpha']);
    http.verify();
  });

  it('visitante que toca no coração vai para o login', () => {
    const http = configurar(false);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const f = TestBed.createComponent(DescobrirPage);
    f.detectChanges();
    responderVitrine(http, false);
    f.detectChanges();
    (f.nativeElement.querySelector('.poster__coracao') as HTMLButtonElement).click();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });
});

describe('Wishlist', () => {
  it('lista por prioridade e reordena ao trocar a prioridade (F-06)', () => {
    const http = configurar(true);
    const f = TestBed.createComponent(WishlistPage);
    f.detectChanges();
    const itens = [
      { jogoId: 'a', titulo: 'Alpha', capaUrl: null, prioridade: 'MEDIA', adicionadoEm: '2026-11-01T10:00:00Z' },
      { jogoId: 'b', titulo: 'Beta', capaUrl: null, prioridade: 'ALTA', adicionadoEm: '2026-11-02T10:00:00Z' },
    ];
    // A tela lê a wishlist e a vitrine também (para o coração dos pôsteres).
    responderVitrine(http, false);
    http.match('/api/perfis/me/wishlist').forEach((pedido) => pedido.flush(itens));
    f.detectChanges();
    const titulos = () => [...f.nativeElement.querySelectorAll('.linha__titulo')].map((e: Element) => e.textContent);
    expect(titulos()).toEqual(['Beta', 'Alpha']);

    const select = f.nativeElement.querySelector('#prioridade-a') as HTMLSelectElement;
    select.value = 'ALTA';
    select.dispatchEvent(new Event('change'));
    f.detectChanges();
    const pedido = http.expectOne('/api/perfis/me/wishlist/a');
    expect(pedido.request.method).toBe('PATCH');
    expect(pedido.request.body).toEqual({ prioridade: 'ALTA' });
    pedido.flush(null);
  });
});

describe('Estatísticas da comunidade', () => {
  it('mostra "—" nos indicadores da ficha oculta e troca a visibilidade pelos chips (F-17 RN 3)', () => {
    const http = configurar(false);
    const f = TestBed.createComponent(EstatisticasPage);
    f.detectChanges();
    http.expectOne((r) => r.url === '/api/estatisticas' && r.params.get('visibilidade') === 'TODOS')
      .flush([ficha('Publico', true, 4.5, 3), ficha('Oculto', false, null, 1)]);
    f.detectChanges();
    const linhas: HTMLTableRowElement[] = [...f.nativeElement.querySelectorAll('tbody tr')];
    expect(linhas[0].textContent).toContain('4,50');
    expect(linhas[0].textContent).toContain('Pública');
    expect(linhas[1].textContent).toContain('—');
    expect(linhas[1].textContent).toContain('Oculta');

    (f.nativeElement.querySelectorAll('.chip')[2] as HTMLButtonElement).click();
    f.detectChanges();
    http.expectOne((r) => r.url === '/api/estatisticas' && r.params.get('visibilidade') === 'OCULTAS').flush([]);
    http.verify();
  });
});

describe('Busca', () => {
  it('sem termo mostra atalhos e não chama a busca', () => {
    const http = configurar(false);
    const f = TestBed.createComponent(BuscaPage);
    f.detectChanges();
    http.expectOne('/api/catalogo/generos').flush([{ id: 'g1', nome: 'RPG' }]);
    f.detectChanges();
    expect(f.nativeElement.textContent).toContain('Buscas em alta');
    http.expectNone('/api/busca');
  });

  it('agrupa jogos e listas encontrados', async () => {
    const http = configurar(false);
    const f = TestBed.createComponent(BuscaPage);
    f.detectChanges();
    http.expectOne('/api/catalogo/generos').flush([]);
    await TestBed.inject(Router).navigate([], { queryParams: { termo: 'elden' } });
    f.detectChanges();
    http.expectOne((r) => r.url === '/api/busca' && r.params.get('termo') === 'elden').flush({
      termo: 'elden',
      jogos: [{ id: 'a', titulo: 'Elden Ring', capaUrl: null, dataLancamento: '2022-02-25' }],
      listas: [{ id: 'l1', titulo: 'Elden favoritos', descricao: null, totalJogos: 2, donoId: 'u2', nomeDoDono: 'Joana' }],
    });
    f.detectChanges();
    const texto = f.nativeElement.textContent as string;
    expect(texto).toContain('2 resultados');
    expect(texto).toContain('Elden Ring');
    expect(texto).toContain('por Joana');
  });
});
