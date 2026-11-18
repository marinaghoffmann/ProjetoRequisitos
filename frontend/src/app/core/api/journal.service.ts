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

export interface AnexoJournal {
  id: string;
  nomeArquivo: string;
  tipoMidia: string;
  tamanho: number;
}

export interface EntradaJournal {
  id: string;
  jogoId: string | null;
  jogoTitulo: string | null;
  jogoCapaUrl: string | null;
  dataSessao: string;
  titulo: string;
  texto: string | null;
  tags: string[];
  anexos: AnexoJournal[];
  contemSpoiler: boolean;
  mascarada: boolean;
  tipoOrigem: TipoOrigem;
  editavel: boolean;
}

export interface DadosEntradaJournal {
  jogoId: string | null;
  dataSessao: string;
  titulo: string;
  texto: string;
  tags: string[];
  contemSpoiler: boolean;
  anexosMantidos: string[];
}

export interface FiltrosJournal {
  jogoId: string;
  tag: string;
  inicio: string;
  fim: string;
  origem: string;
}

@Injectable({ providedIn: 'root' })
export class JournalService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/journal';

  timeline(pagina: number, tamanho: number) {
    const params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    return this.http.get<Pagina<EntradaTimeline>>(`${this.base}/timeline`, { params });
  }

  entradas(filtros: FiltrosJournal, pagina = 1, donoPublico: string | null = null) {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', 20);
    for (const [chave, valor] of Object.entries(filtros)) {
      if (valor) params = params.set(chave, valor);
    }
    const url = donoPublico ? `/api/perfis/${donoPublico}/journal` : `${this.base}/entradas`;
    return this.http.get<Pagina<EntradaJournal>>(url, { params });
  }

  tags(donoPublico: string | null = null) {
    const url = donoPublico ? `/api/perfis/${donoPublico}/journal/tags` : `${this.base}/entradas/tags`;
    return this.http.get<string[]>(url);
  }

  revelar(donoId: string, entradaId: string) {
    return this.http.get<EntradaJournal>(`/api/perfis/${donoId}/journal/${entradaId}`, {
      params: new HttpParams().set('revelarSpoiler', true),
    });
  }

  salvar(dados: DadosEntradaJournal, imagens: File[], id?: string) {
    const corpo = new FormData();
    corpo.append('entrada', new Blob([JSON.stringify(dados)], { type: 'application/json' }));
    for (const imagem of imagens) corpo.append('imagens', imagem, imagem.name);
    return id ? this.http.put<EntradaJournal>(`${this.base}/entradas/${id}`, corpo)
      : this.http.post<EntradaJournal>(`${this.base}/entradas`, corpo);
  }

  excluir(id: string) { return this.http.delete<void>(`${this.base}/entradas/${id}`); }

  imagemUrl(entradaId: string, anexoId: string, revelarSpoiler = false): string {
    return `${this.base}/entradas/${entradaId}/anexos/${anexoId}${revelarSpoiler ? '?revelarSpoiler=true' : ''}`;
  }
}
