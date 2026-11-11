import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type TagComportamento = 'ENVIO_RAPIDO' | 'PRODUTO_COMO_DESCRITO' | 'BOM_COMUNICADOR' | 'PAGAMENTO_RAPIDO';
export type PapelNaNegociacao = 'COMPRADOR' | 'VENDEDOR';

export const TAGS_COMPORTAMENTO: Record<TagComportamento, string> = {
  ENVIO_RAPIDO: 'Envio rápido',
  PRODUTO_COMO_DESCRITO: 'Produto como descrito',
  BOM_COMUNICADOR: 'Bom comunicador',
  PAGAMENTO_RAPIDO: 'Pagamento rápido',
};

export interface AvaliacaoMarketplace {
  id: string;
  pedidoId: string;
  avaliadorId: string;
  nomeAvaliador: string | null;
  avaliadoId: string;
  papelAvaliado: PapelNaNegociacao;
  estrelas: number;
  tags: TagComportamento[];
  comentario: string | null;
  criadaEm: string;
}

export interface PerfilVendedor {
  usuarioId: string;
  nome: string;
  /** null enquanto não houver avaliações. */
  notaMedia: number | null;
  totalAvaliacoes: number;
  negociacoesBemSucedidas: number;
  avaliacoes: AvaliacaoMarketplace[];
}

export interface NovaAvaliacao {
  estrelas: number;
  tags: TagComportamento[];
  comentario: string | null;
}

@Injectable({ providedIn: 'root' })
export class ReputacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/marketplace';

  perfil(usuarioId: string) {
    return this.http.get<PerfilVendedor>(`${this.base}/usuarios/${usuarioId}/reputacao`);
  }

  /** Para a tela do pedido (F-20): quem está logado avalia a outra parte. */
  avaliar(pedidoId: string, avaliacao: NovaAvaliacao) {
    return this.http.post<AvaliacaoMarketplace>(`${this.base}/pedidos/${pedidoId}/avaliacoes`, avaliacao);
  }
}
