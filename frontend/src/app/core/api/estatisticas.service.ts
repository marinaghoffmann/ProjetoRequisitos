import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface MediasCategoria {
  graficos: number | null;
  som: number | null;
  jogabilidade: number | null;
  diversao: number | null;
  inovacao: number | null;
}

/** Enquanto `elegivel` for falso, a API só informa o total de reviews e o mínimo exigido. */
export interface EstatisticaJogo {
  jogoId: string;
  elegivel: boolean;
  totalReviewsAtivas: number;
  minimoDeReviews: number;
  mediaGeral: number | null;
  mediasPorCategoria: MediasCategoria | null;
  totalConclusoes: number | null;
  totalEmWishlists: number | null;
  totalNegociacoes: number | null;
  atualizadaEm: string | null;
}

@Injectable({ providedIn: 'root' })
export class EstatisticasService {
  private readonly http = inject(HttpClient);

  consultar(jogoId: string): Observable<EstatisticaJogo> {
    return this.http.get<EstatisticaJogo>(`/api/catalogo/jogos/${jogoId}/estatisticas`);
  }
}
