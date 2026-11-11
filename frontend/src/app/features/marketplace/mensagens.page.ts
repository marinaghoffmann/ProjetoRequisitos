import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { STATUS_ANUNCIO } from '../../core/api/marketplace.service';
import { NegociacaoService, ResumoConversa, STATUS_PROPOSTA } from '../../core/api/negociacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { urlDaImagem } from './imagem';

/** Caixa de mensagens (F-19): conversas como comprador ou vendedor, com a última proposta e as não lidas. */
@Component({
  selector: 'app-mensagens',
  imports: [DatePipe, RouterLink],
  templateUrl: './mensagens.page.html',
  styleUrl: './mensagens.page.scss',
})
export class MensagensPage {
  private readonly negociacao = inject(NegociacaoService);

  protected readonly statusProposta = STATUS_PROPOSTA;
  protected readonly statusAnuncio = STATUS_ANUNCIO;
  protected readonly imagem = urlDaImagem;
  protected readonly conversas = signal<ResumoConversa[] | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.negociacao.listar().subscribe({
      next: (conversas) => this.conversas.set(conversas),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
