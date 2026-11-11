import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Pagina } from './forum.service';

export type TipoOrigem = 'MANUAL' | 'AUTOMATICO';
export type TipoEventoPlataforma = 'REVIEW_PUBLICADA' | 'JOGO_ZERADO' | 'COMPRA_CONCLUIDA';

export const EVENTOS_PLATAFORMA: Record<TipoEventoPlataforma, string> = {
  REVIEW_PUBLICADA: 'Review publicada',
  JOGO_ZERADO: 'Jogo zerado',
  COMPRA_CONCLUIDA: 'Compra concluída',
};

export interface EntradaTimeline {
  id: string;
  jogoId: string | null;
  jogoTitulo: string | null;
  dataSessao: string;
  texto: string;
  contemSpoiler: boolean;
  tipoOrigem: TipoOrigem;
  /** Só nas entradas automáticas. */
  tipoEvento: TipoEventoPlataforma | null;
  /** Falso nas automáticas: o texto é do sistema (F-16 RN 3). */
  textoEditavel: boolean;
}

@Injectable({ providedIn: 'root' })
export class JournalService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/journal';

  timeline(pagina: number, tamanho: number) {
    const params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    return this.http.get<Pagina<EntradaTimeline>>(`${this.base}/timeline`, { params });
  }
}
