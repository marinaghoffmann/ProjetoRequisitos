import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, tap } from 'rxjs';

import { AutenticacaoService } from '../auth/autenticacao.service';
import { CatalogoService, FiltroCatalogo, Jogo } from '../api/catalogo.service';
import { ResumoBibliotecaService } from '../api/resumo-biblioteca.service';
import { EstatisticaComunidade, EstatisticasService } from '../api/estatisticas.service';
import { WishlistService } from '../api/wishlist.service';
import { anoDe, gradientePara, imagemSegura, plataformasDe } from '../../shared/apresentacao-jogo';

/** Um jogo pronto para o card de pôster: dados do catálogo, nota da F-17 e presença na wishlist. */
export interface JogoNaVitrine {
  jogo: Jogo;
  id: string;
  titulo: string;
  capa: string | null;
  gradiente: string;
  generos: string[];
  ano: string | null;
  plataformas: string[];
  /** Média da comunidade; null enquanto a ficha não for pública (F-17 RN 3). */
  nota: number | null;
  avaliacoes: number;
  naWishlist: boolean;
}

/**
 * Monta a vitrine das telas Descobrir e Jogos com uma carga só: catálogo, gêneros, a tabela de estatísticas
 * (para a nota) e a wishlist do usuário. Falhas nas partes secundárias não derrubam a vitrine.
 */
@Injectable({ providedIn: 'root' })
export class VitrineService {
  private readonly catalogo = inject(CatalogoService);
  private readonly estatisticas = inject(EstatisticasService);
  private readonly wishlist = inject(WishlistService);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly resumo = inject(ResumoBibliotecaService);

  /** Coração do pôster: adiciona (prioridade padrão, F-06 RN 3) ou remove; devolve o novo estado. */
  alternarWishlist(jogoId: string, estaNaWishlist: boolean): Observable<boolean> {
    const pedido = estaNaWishlist ? this.wishlist.remover(jogoId) : this.wishlist.adicionar(jogoId);
    return pedido.pipe(map(() => !estaNaWishlist), tap(() => this.resumo.atualizar()));
  }

  carregar(filtro: FiltroCatalogo = {}): Observable<JogoNaVitrine[]> {
    const logado = !!this.autenticacao.usuarioAtual();
    return forkJoin({
      jogos: this.catalogo.listarJogos(filtro),
      generos: this.catalogo.listarGeneros().pipe(catchError(() => of([]))),
      fichas: this.estatisticas.comunidade('TODOS').pipe(catchError(() => of([] as EstatisticaComunidade[]))),
      wishlist: logado ? this.wishlist.listar().pipe(catchError(() => of([]))) : of([]),
    }).pipe(
      map(({ jogos, generos, fichas, wishlist }) => {
        const nomes = new Map(generos.map((g) => [g.id, g.nome]));
        const porJogo = new Map(fichas.map((f) => [f.jogoId, f]));
        const desejados = new Set(wishlist.map((w) => w.jogoId));
        return jogos.map((jogo) => {
          const ficha = porJogo.get(jogo.id);
          return {
            jogo,
            id: jogo.id,
            titulo: jogo.titulo,
            capa: imagemSegura(jogo.capaUrl),
            gradiente: gradientePara(jogo.id),
            generos: jogo.generosIds.map((id) => nomes.get(id)).filter((n): n is string => !!n).sort(),
            ano: anoDe(jogo.dataLancamento),
            plataformas: plataformasDe(jogo),
            nota: ficha?.publica ? ficha.mediaGeral : null,
            avaliacoes: ficha?.totalReviewsAtivas ?? 0,
            naWishlist: desejados.has(jogo.id),
          };
        });
      }),
    );
  }
}
