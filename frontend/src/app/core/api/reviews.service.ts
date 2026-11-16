import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

export type CategoriaReview = 'GRAFICOS' | 'SOM' | 'JOGABILIDADE' | 'DIVERSAO' | 'INOVACAO';

export const CATEGORIAS_REVIEW: { chave: CategoriaReview; rotulo: string }[] = [
  { chave: 'GRAFICOS', rotulo: 'Gráficos' },
  { chave: 'SOM', rotulo: 'Som' },
  { chave: 'JOGABILIDADE', rotulo: 'Jogabilidade' },
  { chave: 'DIVERSAO', rotulo: 'Diversão' },
  { chave: 'INOVACAO', rotulo: 'Inovação' },
];

export const LIMITE_TEXTO_REVIEW = 5000;

export interface NotaCategoria {
  categoria: CategoriaReview;
  nota: number;
}

export interface Review {
  id: string;
  autorId: string;
  nomeAutor: string | null;
  jogoId: string;
  /** Só nas listas de reviews de vários jogos (perfil). */
  jogoTitulo: string | null;
  jogoCapaUrl: string | null;
  notaGeral: number;
  notasPorCategoria: NotaCategoria[];
  texto: string;
  criadaEm: string;
  atualizadaEm: string;
  editada: boolean;
}

export interface MediaCategoria {
  categoria: CategoriaReview;
  media: number | null;
  totalNotas: number;
}

export interface PaginaReviews {
  itens: Review[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

/** As médias consideram todas as reviews do jogo, e não só as da página. */
export interface ReviewsDoJogo {
  jogoId: string;
  totalReviews: number;
  mediaGeral: number | null;
  mediasPorCategoria: MediaCategoria[];
  reviews: PaginaReviews;
}

export interface DadosReview {
  jogoId: string;
  notaGeral: number;
  notasPorCategoria: NotaCategoria[];
  texto: string;
}

@Injectable({ providedIn: 'root' })
export class ReviewsService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/avaliacao';

  doJogo(jogoId: string, pagina = 1, tamanho = 10): Observable<ReviewsDoJogo> {
    const params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    return this.http.get<ReviewsDoJogo>(`${this.base}/jogos/${jogoId}/reviews`, { params });
  }

  /** A review do usuário logado para o jogo, ou null se ele ainda não avaliou (204). */
  minhaDoJogo(jogoId: string): Observable<Review | null> {
    return this.http
      .get<Review>(`${this.base}/jogos/${jogoId}/reviews/minha`, { observe: 'response' })
      .pipe(map((resposta: HttpResponse<Review>) => resposta.body ?? null));
  }

  minhas(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.base}/reviews/minhas`);
  }

  /** RN 1: se o usuário já avaliou o jogo, a API responde 409 (ErroApi.status) e a tela abre a edição. */
  publicar(dados: DadosReview): Observable<Review> {
    return this.http.post<Review>(`${this.base}/reviews`, dados);
  }

  editar(id: string, dados: DadosReview): Observable<Review> {
    return this.http.put<Review>(`${this.base}/reviews/${id}`, dados);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/reviews/${id}`);
  }
}
