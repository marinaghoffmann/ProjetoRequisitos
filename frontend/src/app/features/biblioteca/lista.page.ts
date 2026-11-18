import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ItemDaLista, LIMITE_NOTA_ITEM, ListaPersonalizada, ListasService } from '../../core/api/listas.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

import { ConteudoModeravelComponent } from '../moderacao/conteudo-moderavel.component';

/**
 * Uma lista (F-04): os jogos com capa, a nota explicativa e a data de inclusão, mais o contador "N/100".
 * Qualquer um pode ver; só o dono edita as notas e remove itens (o backend confere de novo).
 */
@Component({
  selector: 'app-lista',
  imports: [ConteudoModeravelComponent, DatePipe, RouterLink],
  templateUrl: './lista.page.html',
  styleUrl: './lista.page.scss',
})
export class ListaPage {
  private readonly servico = inject(ListasService);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';

  protected readonly limiteNota = LIMITE_NOTA_ITEM;
  protected readonly lista = signal<ListaPersonalizada | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroItem = signal<string | null>(null);
  /** Rascunhos das notas em edição, por jogo. */
  protected readonly rascunhos = signal<Record<string, string>>({});
  protected readonly salvo = signal<string | null>(null);

  protected readonly ehDono = computed(() => this.lista()?.donoId === this.autenticacao.usuarioAtual()?.id);

  constructor() {
    this.servico.buscar(this.id).subscribe({
      next: (lista) => this.atualizar(lista),
      error: (e: ErroApi) => this.erro.set(e.status === 404 ? 'Lista não encontrada.' : e.mensagem),
    });
  }

  protected nota(item: ItemDaLista): string {
    return this.rascunhos()[item.jogoId] ?? item.notaExplicativa;
  }

  protected escrever(item: ItemDaLista, texto: string): void {
    this.rascunhos.update((atuais) => ({ ...atuais, [item.jogoId]: texto }));
    this.salvo.set(null);
  }

  protected alterada(item: ItemDaLista): boolean {
    const rascunho = this.rascunhos()[item.jogoId];
    return rascunho !== undefined && rascunho !== item.notaExplicativa;
  }

  protected salvarNota(item: ItemDaLista): void {
    this.erroItem.set(null);
    this.servico.editarNota(this.id, item.jogoId, this.nota(item)).subscribe({
      next: (lista) => {
        this.atualizar(lista);
        this.salvo.set(item.jogoId);
      },
      error: (e: ErroApi) => this.erroItem.set(e.mensagem),
    });
  }

  protected remover(item: ItemDaLista): void {
    this.erroItem.set(null);
    this.servico.removerJogo(this.id, item.jogoId).subscribe({
      next: (lista) => this.atualizar(lista),
      error: (e: ErroApi) => this.erroItem.set(e.mensagem),
    });
  }

  private atualizar(lista: ListaPersonalizada): void {
    this.lista.set(lista);
    this.rascunhos.set({});
  }
}
