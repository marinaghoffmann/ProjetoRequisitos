import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import {
  Anuncio,
  ESTADOS,
  MarketplaceService,
  STATUS_ANUNCIO,
  StatusAnuncio,
  TIPOS_NEGOCIACAO,
} from '../../core/api/marketplace.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-meus-anuncios',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './meus-anuncios.page.html',
  styleUrl: './marketplace.page.scss',
})
export class MeusAnunciosPage {
  private readonly marketplace = inject(MarketplaceService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly estados = ESTADOS;
  protected readonly tipos = TIPOS_NEGOCIACAO;
  protected readonly status = STATUS_ANUNCIO;
  protected readonly anuncios = signal<Anuncio[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    if (this.autenticacao.usuarioAtual()) {
      this.carregar();
    } else {
      this.carregando.set(false);
    }
  }

  /** Vendidos e cancelados não mudam mais; em negociação, quem decide é o pedido (F-20). */
  protected editavel(anuncio: Anuncio): boolean {
    return anuncio.status === 'ATIVO' || anuncio.status === 'PAUSADO';
  }

  protected imagemUrl(caminho: string): string {
    return caminho.startsWith('/uploads/') || /^https?:\/\//.test(caminho) ? caminho : '';
  }

  protected alterarStatus(anuncio: Anuncio, novo: StatusAnuncio): void {
    this.marketplace.alterarStatus(anuncio.id, novo).subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected excluir(anuncio: Anuncio): void {
    if (!confirm(`Excluir o anúncio "${anuncio.titulo}"? Ele deixará de aparecer no marketplace.`)) return;
    this.marketplace.excluir(anuncio.id).subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.marketplace.meusAnuncios().subscribe({
      next: (anuncios) => {
        this.anuncios.set(anuncios);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }
}
