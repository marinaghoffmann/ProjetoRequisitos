import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CabecalhoPaginaComponent } from '../../shared/ui/cabecalho-pagina.component';
import { CarregandoComponent, EstadoVazioComponent } from '../../shared/ui/estados.component';

import { STATUS_ANUNCIO } from '../../core/api/marketplace.service';
import { NegociacaoService, ResumoConversa, STATUS_PROPOSTA } from '../../core/api/negociacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { urlDaImagem } from './imagem';

/**
 * Caixa de mensagens (F-19) em duas colunas: as conversas como comprador ou vendedor, com a última proposta e as
 * não lidas, e a conversa aberta à direita. No celular aparece uma coluna por vez.
 */
@Component({
  selector: 'app-mensagens',
  imports: [DatePipe, RouterLink, RouterLinkActive, RouterOutlet, CabecalhoPaginaComponent, CarregandoComponent, EstadoVazioComponent],
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
  /** Há uma conversa aberta na coluna da direita (rota filha :id). */
  protected readonly aberta = signal(false);

  constructor() {
    this.negociacao.listar().subscribe({
      next: (conversas) => this.conversas.set(conversas),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
