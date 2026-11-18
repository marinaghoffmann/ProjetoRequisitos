import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface ItemWishlist {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/perfis/me/wishlist';

  listar() {
    return this.http.get<ItemWishlist[]>(this.base);
  }
  adicionar(jogoId: string) {
    return this.http.post<void>(this.base, { jogoId });
  }
  remover(jogoId: string) {
    return this.http.delete<void>(`${this.base}/${jogoId}`);
  }
}