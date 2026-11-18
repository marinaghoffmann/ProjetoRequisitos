import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface Favorito {
  slot: number;
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
}
export interface PerfilPublico {
  id: string;
  nome: string;
  bio: string | null;
  localizacao: string | null;
  avatarUrl: string | null;
  bannerUrl: string | null;
  favoritos: Favorito[];
}

@Injectable({ providedIn: 'root' })
export class PerfilPublicoService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/perfis';

  consultar(id: string) {
    return this.http.get<PerfilPublico>(`${this.base}/${id}`);
  }
  adicionar(jogoId: string, slot?: number) {
    return this.http.post<void>(`${this.base}/me/favoritos`, { jogoId, slot });
  }
  substituir(slot: number, jogoId: string) {
    return this.http.put<void>(`${this.base}/me/favoritos/slots/${slot}`, { jogoId });
  }
  mover(jogoId: string, slot: number) {
    return this.http.patch<void>(`${this.base}/me/favoritos/${jogoId}/posicao`, { slot });
  }
  remover(jogoId: string) {
    return this.http.delete<void>(`${this.base}/me/favoritos/${jogoId}`);
  }
}
