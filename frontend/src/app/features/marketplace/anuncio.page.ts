import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

import {
  Anuncio,
  ESTADOS,
  MIDIAS,
  MarketplaceService,
  STATUS_ANUNCIO,
  TIPOS_NEGOCIACAO,
} from '../../core/api/marketplace.service';
import { NegociacaoService, NovaProposta } from '../../core/api/negociacao.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { PropostaDialogComponent } from './proposta-dialog.component';
import { PerfilVendedor, ReputacaoService } from '../../core/api/reputacao.service';
import { BadgeStatusComponent } from '../../shared/ui/badge-status.component';
import { IconeComponent } from '../../shared/ui/icone.component';
import { urlDaImagem } from './imagem';

import { ConteudoModeravelComponent } from '../moderacao/conteudo-moderavel.component';

/**
 * Página do anúncio (pendência da F-18 resolvida pela F-19): fotos e dados do item e, para quem não é o
 * dono, os botões Conversar, Fazer proposta e Comprar.
 */
@Component({
  selector: 'app-anuncio',
  imports: [ConteudoModeravelComponent, CurrencyPipe, RouterLink, PropostaDialogComponent, DecimalPipe, BadgeStatusComponent, IconeComponent],
  templateUrl: './anuncio.page.html',
  styleUrl: './anuncio.page.scss',
})
export class AnuncioPage {
  private readonly marketplace = inject(MarketplaceService);
  private readonly negociacao = inject(NegociacaoService);
  private readonly router = inject(Router);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly estados = ESTADOS;
  protected readonly tipos = TIPOS_NEGOCIACAO;
  protected readonly midias = MIDIAS;
  protected readonly statusAnuncio = STATUS_ANUNCIO;
  protected readonly imagem = urlDaImagem;

  protected readonly anuncio = signal<Anuncio | null>(null);
  /** Reputação do vendedor (F-21); some se a consulta falhar. */
  protected readonly vendedor = signal<PerfilVendedor | null>(null);
  private readonly reputacao = inject(ReputacaoService);
  protected readonly fotoAtual = signal(0);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroAcao = signal<string | null>(null);
  protected readonly dialogoAberto = signal(false);
  protected readonly enviando = signal(false);

  protected readonly dono = computed(() => {
    const anuncio = this.anuncio();
    return !!anuncio && anuncio.vendedorId === this.autenticacao.usuarioAtual()?.id;
  });
  protected readonly negociavel = computed(() => this.anuncio()?.status === 'ATIVO');
  protected readonly compraDireta = computed(() => this.negociavel() && this.anuncio()?.tipoNegociacao !== 'TROCA');

  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';
    this.marketplace.buscarAnuncio(id).subscribe({
      next: (anuncio) => {
        this.anuncio.set(anuncio);
        this.reputacao.perfil(anuncio.vendedorId).subscribe({ next: (v) => this.vendedor.set(v), error: () => {} });
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected textoNegociacao(tipo: Anuncio['tipoNegociacao']): string {
    return tipo === 'VENDA' ? 'Aceita venda' : tipo === 'TROCA' ? 'Aceita troca' : 'Aceita venda ou troca';
  }

  /** RN 1 e RN 2: abre (ou reabre) a conversa com o vendedor. */
  protected conversar(): void {
    const anuncio = this.anuncio();
    if (!anuncio) return;
    this.erroAcao.set(null);
    this.negociacao.iniciar(anuncio.id).subscribe({
      next: (conversa) => this.router.navigate(['/marketplace/mensagens', conversa.id]),
      error: (e: ErroApi) => this.erroAcao.set(e.mensagem),
    });
  }

  protected abrirProposta(): void {
    this.erroAcao.set(null);
    this.dialogoAberto.set(true);
  }

  /** A proposta vai dentro da conversa: abre (ou reabre) a conversa e envia nela. */
  protected enviarProposta(proposta: NovaProposta): void {
    const anuncio = this.anuncio();
    if (!anuncio) return;
    this.enviando.set(true);
    this.erroAcao.set(null);
    this.negociacao
      .iniciar(anuncio.id)
      .pipe(switchMap((conversa) => this.negociacao.enviarProposta(conversa.id, proposta).pipe(
        switchMap(() => this.router.navigate(['/marketplace/mensagens', conversa.id])),
      )))
      .subscribe({
        error: (e: ErroApi) => {
          this.erroAcao.set(e.mensagem);
          this.enviando.set(false);
        },
      });
  }

  protected comprar(): void {
    const anuncio = this.anuncio();
    if (anuncio) this.router.navigate(['/marketplace/checkout'], { queryParams: { anuncio: anuncio.id } });
  }
}
