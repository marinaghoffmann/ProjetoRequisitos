import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

/** F-06 RN 3: sem prioridade informada, o backend usa MEDIA. */
export type PrioridadeWishlist = 'ALTA' | 'MEDIA' | 'BAIXA';

export const ROTULOS_PRIORIDADE: Record<PrioridadeWishlist, string> = {
  ALTA: 'Alta',
  MEDIA: 'Média',
  BAIXA: 'Baixa',
};

/** A API devolve a wishlist já ordenada: ALTA, MEDIA, BAIXA e, na mesma prioridade, a mais recente. */
export interface ItemWishlist {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
  prioridade: PrioridadeWishlist;
  adicionadoEm: string;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/perfis/me/wishlist';

  listar() {
    return this.http.get<ItemWishlist[]>(this.base);
  }
  listarDe(usuarioId: string) {
    return this.http.get<ItemWishlist[]>(`/api/perfis/${usuarioId}/wishlist`);
  }
  adicionar(jogoId: string, prioridade?: PrioridadeWishlist) {
    return this.http.post<void>(this.base, { jogoId, prioridade: prioridade ?? null });
  }
  alterarPrioridade(jogoId: string, prioridade: PrioridadeWishlist) {
    return this.http.patch<void>(`${this.base}/${jogoId}`, { prioridade });
  }
  remover(jogoId: string) {
    return this.http.delete<void>(`${this.base}/${jogoId}`);
  }
}
