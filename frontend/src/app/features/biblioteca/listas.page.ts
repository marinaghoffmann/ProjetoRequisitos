import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  LIMITE_DESCRICAO_LISTA, LIMITE_JOGOS_LISTA, LIMITE_TITULO_LISTA, ListaPersonalizada, ListasService,
} from '../../core/api/listas.service';
import { ErroApi } from '../../core/http/erro-api';

/**
 * "Minhas listas" (F-04): as listas do usuário com o total de jogos, mais criar, renomear (título e
 * descrição) e excluir. RN 1 (título repetido) vem da API, na mensagem do erro.
 */
@Component({
  selector: 'app-listas',
  imports: [RouterLink],
  templateUrl: './listas.page.html',
  styleUrl: './listas.page.scss',
})
export class ListasPage {
  private readonly servico = inject(ListasService);

  protected readonly limiteJogos = LIMITE_JOGOS_LISTA;
  protected readonly limiteTitulo = LIMITE_TITULO_LISTA;
  protected readonly limiteDescricao = LIMITE_DESCRICAO_LISTA;

  protected readonly listas = signal<ListaPersonalizada[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  // Nova lista
  protected readonly titulo = signal('');
  protected readonly descricao = signal('');
  protected readonly erroCriacao = signal<string | null>(null);
  protected readonly salvando = signal(false);

  // Renomear / excluir
  protected readonly editandoId = signal<string | null>(null);
  protected readonly novoTitulo = signal('');
  protected readonly novaDescricao = signal('');
  protected readonly erroEdicao = signal<string | null>(null);
  protected readonly confirmandoExclusao = signal<string | null>(null);

  constructor() {
    this.carregar();
  }

  protected criar(): void {
    this.salvando.set(true);
    this.erroCriacao.set(null);
    this.servico.criar({ titulo: this.titulo(), descricao: this.descricao() }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.titulo.set('');
        this.descricao.set('');
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.salvando.set(false);
        this.erroCriacao.set(e.mensagem);
      },
    });
  }

  protected iniciarEdicao(lista: ListaPersonalizada): void {
    this.editandoId.set(lista.id);
    this.novoTitulo.set(lista.titulo);
    this.novaDescricao.set(lista.descricao);
    this.erroEdicao.set(null);
  }

  protected salvarEdicao(id: string): void {
    this.servico.editar(id, { titulo: this.novoTitulo(), descricao: this.novaDescricao() }).subscribe({
      next: () => {
        this.editandoId.set(null);
        this.carregar();
      },
      error: (e: ErroApi) => this.erroEdicao.set(e.mensagem),
    });
  }

  protected excluir(id: string): void {
    this.servico.excluir(id).subscribe({
      next: () => {
        this.confirmandoExclusao.set(null);
        this.carregar();
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  /** Até quatro capas, para o card da lista. */
  protected capas(lista: ListaPersonalizada): string[] {
    return lista.itens
      .map((item) => item.jogoCapaUrl)
      .filter((capa): capa is string => !!capa)
      .slice(0, 4);
  }

  private carregar(): void {
    this.servico.minhas().subscribe({
      next: (listas) => {
        this.listas.set(listas);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }
}
