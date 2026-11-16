import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type TipoFeedback = 'UTIL' | 'NAO_RELEVANTE';
export type OrigemSimilaridade = 'AUTOMATICA' | 'MANUAL';

/** Um jogo similar ao jogo consultado. `grau` já vem rebaixado quando a sugestão foi marcada como pouco relevante. */
export interface JogoSimilar {
  similaridadeId: string;
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
  grau: number;
  grauBase: number;
  origem: OrigemSimilaridade;
  rebaixada: boolean;
  totalUteis: number;
  totalNaoRelevantes: number;
  meuFeedback: TipoFeedback | null;
  calculadaEm: string;
}

export interface Similaridade {
  id: string;
  jogoAId: string;
  jogoBId: string;
  grau: number;
  grauBase: number;
  origem: OrigemSimilaridade;
  status: 'ATIVA' | 'REMOVIDA';
  rebaixada: boolean;
  totalUteis: number;
  totalNaoRelevantes: number;
  calculadaEm: string;
}

@Injectable({ providedIn: 'root' })
export class SimilaridadeService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/descoberta';

  similares(jogoId: string, limite = 8, notaMinima?: number | null): Observable<JogoSimilar[]> {
    let params = new HttpParams().set('limite', limite);
    if (notaMinima != null) params = params.set('notaMinima', notaMinima);
    return this.http.get<JogoSimilar[]>(`${this.base}/jogos/${jogoId}/similares`, { params });
  }

  registrarFeedback(similaridadeId: string, tipo: TipoFeedback): Observable<Similaridade> {
    return this.http.post<Similaridade>(`${this.base}/similaridades/${similaridadeId}/feedback`, { tipo });
  }

  editarFeedback(similaridadeId: string, tipo: TipoFeedback): Observable<Similaridade> {
    return this.http.put<Similaridade>(`${this.base}/similaridades/${similaridadeId}/feedback`, { tipo });
  }

  removerFeedback(similaridadeId: string): Observable<Similaridade> {
    return this.http.delete<Similaridade>(`${this.base}/similaridades/${similaridadeId}/feedback`);
  }

  /** Só administradores. */
  criarManual(jogoAId: string, jogoBId: string, grau: number): Observable<Similaridade> {
    return this.http.post<Similaridade>(`${this.base}/similaridades`, { jogoAId, jogoBId, grau });
  }

  /** Só administradores: a conexão some das sugestões e não volta no recálculo automático. */
  remover(similaridadeId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/similaridades/${similaridadeId}`);
  }

  /** Só administradores. */
  recalcular(jogoId: string): Observable<Similaridade[]> {
    return this.http.post<Similaridade[]>(`${this.base}/jogos/${jogoId}/similares/recalcular`, {});
  }
}
