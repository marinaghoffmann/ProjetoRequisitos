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
  totalJogos: number;
  totalZerados: number;
}
export interface JogoPublico {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
}
export interface ZeradoPublico {
  jogoId: string;
  titulo: string;
  totalConclusoes: number;
  ultimaConclusao: string;
}

@Injectable({ providedIn: 'root' })
export class PerfilPublicoService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/perfis';

  consultar(id: string) {
    return this.http.get<PerfilPublico>(`${this.base}/${id}`);
  }
  biblioteca(id: string) {
    return this.http.get<JogoPublico[]>(`${this.base}/${id}/biblioteca`);
  }
  zerados(id: string) {
    return this.http.get<ZeradoPublico[]>(`${this.base}/${id}/zerados`);
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
