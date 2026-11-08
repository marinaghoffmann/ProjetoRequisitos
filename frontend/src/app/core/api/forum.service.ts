import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type StatusTopico = 'ABERTO' | 'RESOLVIDO' | 'ENCERRADO';

export const STATUS_TOPICO: Record<StatusTopico, string> = {
  ABERTO: 'Aberto',
  RESOLVIDO: 'Resolvido',
  ENCERRADO: 'Encerrado',
};

export interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

export interface ResumoTopico {
  id: string;
  autorId: string;
  nomeAutor: string | null;
  jogoId: string | null;
  categoria: string;
  titulo: string;
  status: StatusTopico;
  totalRespostas: number;
  dataUltimaAtividade: string;
  publicadoEm: string;
}

export interface RespostaForum {
  id: string;
  autorId: string;
  nomeAutor: string | null;
  /** null: resposta direta ao tópico. */
  respostaPaiId: string | null;
  /** null quando a resposta foi removida. */
  conteudo: string | null;
  removida: boolean;
  publicadaEm: string;
  editadaEm: string | null;
  editavelAte: string;
}

export interface Topico {
  id: string;
  autorId: string;
  nomeAutor: string | null;
  jogoId: string | null;
  categoria: string;
  titulo: string;
  conteudo: string;
  status: StatusTopico;
  totalRespostas: number;
  publicadoEm: string;
  editadoEm: string | null;
  editavelAte: string;
  dataUltimaAtividade: string;
  respostas: RespostaForum[];
}

export interface DadosTopico {
  /** Ignorado na edição: o tópico continua no mesmo jogo. */
  jogoId: string | null;
  categoria: string;
  titulo: string;
  conteudo: string;
}

export interface FiltroTopicos {
  jogoId?: string;
  categoria?: string;
  pagina?: number;
  tamanho?: number;
}

@Injectable({ providedIn: 'root' })
export class ForumService {
  private readonly http = inject(HttpClient);

  listar(filtro: FiltroTopicos = {}): Observable<Pagina<ResumoTopico>> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    }
    return this.http.get<Pagina<ResumoTopico>>('/api/forum/topicos', { params });
  }

  buscarTopico(id: string): Observable<Topico> {
    return this.http.get<Topico>(`/api/forum/topicos/${id}`);
  }

  criar(dados: DadosTopico): Observable<Topico> {
    return this.http.post<Topico>('/api/forum/topicos', dados);
  }

  editar(id: string, dados: DadosTopico): Observable<Topico> {
    return this.http.put<Topico>(`/api/forum/topicos/${id}`, dados);
  }

  alterarStatus(id: string, status: StatusTopico): Observable<Topico> {
    return this.http.patch<Topico>(`/api/forum/topicos/${id}/status`, { status });
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`/api/forum/topicos/${id}`);
  }

  responder(topicoId: string, conteudo: string, respostaPaiId: string | null = null): Observable<Topico> {
    return this.http.post<Topico>(`/api/forum/topicos/${topicoId}/respostas`, { conteudo, respostaPaiId });
  }

  editarResposta(topicoId: string, respostaId: string, conteudo: string): Observable<Topico> {
    return this.http.put<Topico>(`/api/forum/topicos/${topicoId}/respostas/${respostaId}`, { conteudo });
  }

  excluirResposta(topicoId: string, respostaId: string): Observable<Topico> {
    return this.http.delete<Topico>(`/api/forum/topicos/${topicoId}/respostas/${respostaId}`);
  }
}
