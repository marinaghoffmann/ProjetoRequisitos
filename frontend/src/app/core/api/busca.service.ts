import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface JogoEncontrado {
  id: string;
  titulo: string;
  capaUrl: string | null;
  dataLancamento: string | null;
}

export interface ListaEncontrada {
  id: string;
  titulo: string;
  descricao: string | null;
  totalJogos: number;
  donoId: string;
  nomeDoDono: string | null;
}

/** Busca global: jogos ativos e listas pelo título. Não há busca de pessoas (sem consulta pública de usuários). */
export interface ResultadoBusca {
  termo: string;
  jogos: JogoEncontrado[];
  listas: ListaEncontrada[];
}

export const TAMANHO_MINIMO_DO_TERMO = 2;

@Injectable({ providedIn: 'root' })
export class BuscaService {
  private readonly http = inject(HttpClient);

  buscar(termo: string): Observable<ResultadoBusca> {
    return this.http.get<ResultadoBusca>('/api/busca', { params: { termo } });
  }
}
