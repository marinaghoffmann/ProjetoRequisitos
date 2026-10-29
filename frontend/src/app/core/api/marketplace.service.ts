import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type EstadoConservacao = 'NOVO' | 'EXCELENTE' | 'USADO' | 'COM_MARCAS';
export type TipoNegociacao = 'VENDA' | 'TROCA' | 'AMBOS';
export type MidiaJogo = 'FISICO' | 'DIGITAL';
export type StatusAnuncio = 'ATIVO' | 'PAUSADO' | 'EM_NEGOCIACAO' | 'VENDIDO' | 'CANCELADO';

export const ESTADOS: Record<EstadoConservacao, string> = {
  NOVO: 'Novo',
  EXCELENTE: 'Excelente',
  USADO: 'Usado',
  COM_MARCAS: 'Com marcas',
};

export const TIPOS_NEGOCIACAO: Record<TipoNegociacao, string> = {
  VENDA: 'Venda',
  TROCA: 'Troca',
  AMBOS: 'Venda ou troca',
};

export const MIDIAS: Record<MidiaJogo, string> = {
  FISICO: 'Físico',
  DIGITAL: 'Digital',
};

export const STATUS_ANUNCIO: Record<StatusAnuncio, string> = {
  ATIVO: 'Ativo',
  PAUSADO: 'Pausado',
  EM_NEGOCIACAO: 'Em negociação',
  VENDIDO: 'Vendido',
  CANCELADO: 'Cancelado',
};

export interface Anuncio {
  id: string;
  vendedorId: string;
  jogoId: string;
  titulo: string;
  estado: EstadoConservacao;
  preco: number;
  tipoNegociacao: TipoNegociacao;
  midia: MidiaJogo;
  localidade: string;
  fotos: string[];
  status: StatusAnuncio;
  criadoEm: string;
  atualizadoEm: string;
}

export interface DadosAnuncio {
  /** Ignorado na edição: o anúncio continua vinculado ao mesmo jogo. */
  jogoId: string | null;
  titulo: string;
  estado: EstadoConservacao;
  preco: number;
  tipoNegociacao: TipoNegociacao;
  midia: MidiaJogo;
  localidade: string;
  fotos: string[];
}

export interface FiltroAnuncios {
  jogoId?: string;
  titulo?: string;
  precoMinimo?: number | null;
  precoMaximo?: number | null;
  estado?: EstadoConservacao | '';
  tipoNegociacao?: TipoNegociacao | '';
  regiao?: string;
}

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private readonly http = inject(HttpClient);

  buscar(filtro: FiltroAnuncios = {}): Observable<Anuncio[]> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    }
    return this.http.get<Anuncio[]>('/api/marketplace/anuncios', { params });
  }

  meusAnuncios(): Observable<Anuncio[]> {
    return this.http.get<Anuncio[]>('/api/marketplace/anuncios/meus');
  }

  buscarAnuncio(id: string): Observable<Anuncio> {
    return this.http.get<Anuncio>(`/api/marketplace/anuncios/${id}`);
  }

  criar(dados: DadosAnuncio): Observable<Anuncio> {
    return this.http.post<Anuncio>('/api/marketplace/anuncios', dados);
  }

  editar(id: string, dados: DadosAnuncio): Observable<Anuncio> {
    return this.http.put<Anuncio>(`/api/marketplace/anuncios/${id}`, dados);
  }

  alterarStatus(id: string, status: StatusAnuncio): Observable<Anuncio> {
    return this.http.patch<Anuncio>(`/api/marketplace/anuncios/${id}/status`, { status });
  }

  excluir(id: string): Observable<Anuncio> {
    return this.http.delete<Anuncio>(`/api/marketplace/anuncios/${id}`);
  }

  enviarFoto(imagem: File): Observable<{ caminho: string }> {
    const dados = new FormData();
    dados.append('imagem', imagem);
    return this.http.post<{ caminho: string }>('/api/marketplace/fotos', dados);
  }
}
