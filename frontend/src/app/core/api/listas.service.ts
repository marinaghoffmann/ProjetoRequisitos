import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

/** RN 3: no máximo 100 jogos por lista (a API também informa em `limiteDeJogos`). */
export const LIMITE_JOGOS_LISTA = 100;
export const LIMITE_TITULO_LISTA = 80;
export const LIMITE_DESCRICAO_LISTA = 500;
export const LIMITE_NOTA_ITEM = 500;

export interface ItemDaLista {
  itemId: string;
  jogoId: string;
  jogoTitulo: string | null;
  jogoCapaUrl: string | null;
  jogoStatus: string | null;
  notaExplicativa: string;
  incluidoEm: string;
}

export interface ListaPersonalizada {
  id: string;
  donoId: string;
  nomeDono: string | null;
  titulo: string;
  descricao: string;
  totalJogos: number;
  limiteDeJogos: number;
  criadaEm: string;
  atualizadaEm: string;
  itens: ItemDaLista[];
}

export interface DadosLista {
  titulo: string;
  descricao: string;
}

@Injectable({ providedIn: 'root' })
export class ListasService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/biblioteca/listas';

  minhas(): Observable<ListaPersonalizada[]> {
    return this.http.get<ListaPersonalizada[]>(this.base);
  }

  doUsuario(usuarioId: string): Observable<ListaPersonalizada[]> {
    return this.http.get<ListaPersonalizada[]>(`/api/biblioteca/usuarios/${usuarioId}/listas`);
  }

  buscar(id: string): Observable<ListaPersonalizada> {
    return this.http.get<ListaPersonalizada>(`${this.base}/${id}`);
  }

  criar(dados: DadosLista): Observable<ListaPersonalizada> {
    return this.http.post<ListaPersonalizada>(this.base, dados);
  }

  editar(id: string, dados: DadosLista): Observable<ListaPersonalizada> {
    return this.http.put<ListaPersonalizada>(`${this.base}/${id}`, dados);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  adicionarJogo(id: string, jogoId: string, notaExplicativa: string): Observable<ListaPersonalizada> {
    return this.http.post<ListaPersonalizada>(`${this.base}/${id}/itens`, { jogoId, notaExplicativa });
  }

  editarNota(id: string, jogoId: string, notaExplicativa: string): Observable<ListaPersonalizada> {
    return this.http.patch<ListaPersonalizada>(`${this.base}/${id}/itens/${jogoId}`, { notaExplicativa });
  }

  removerJogo(id: string, jogoId: string): Observable<ListaPersonalizada> {
    return this.http.delete<ListaPersonalizada>(`${this.base}/${id}/itens/${jogoId}`);
  }
}
