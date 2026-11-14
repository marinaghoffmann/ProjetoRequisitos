import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, interval } from 'rxjs';

import { STATUS_ANUNCIO } from '../../core/api/marketplace.service';
import { NaoLidasService } from '../../core/api/nao-lidas.service';
import {
  Conversa,
  MensagemChat,
  NegociacaoService,
  NovaProposta,
  PropostaNegociacao,
  STATUS_PROPOSTA,
} from '../../core/api/negociacao.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';
import { PropostaDialogComponent } from './proposta-dialog.component';
import { urlDaImagem } from './imagem';

type ItemDaConversa =
  | { tipo: 'mensagem'; quando: string; mensagem: MensagemChat }
  | { tipo: 'proposta'; quando: string; proposta: PropostaNegociacao };

/** Intervalo da consulta periódica: o chat é assíncrono, sem WebSocket. */
const ATUALIZAR_A_CADA_MS = 5000;

/**
 * Conversa aberta (F-19): mensagens e propostas em ordem cronológica. O vendedor aceita ou recusa a
 * proposta pendente; o comprador cancela, e depois do aceite segue para o pagamento dentro de 48 horas.
 */
@Component({
  selector: 'app-conversa',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, PropostaDialogComponent],
  templateUrl: './conversa.page.html',
  styleUrl: './conversa.page.scss',
})
export class ConversaPage {
  private readonly negociacao = inject(NegociacaoService);
  private readonly router = inject(Router);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly naoLidas = inject(NaoLidasService);
  private readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';

  protected readonly statusProposta = STATUS_PROPOSTA;
  protected readonly statusAnuncio = STATUS_ANUNCIO;
  protected readonly imagem = urlDaImagem;

  protected readonly conversa = signal<Conversa | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroAcao = signal<string | null>(null);
  protected readonly dialogoAberto = signal(false);
  protected readonly ocupado = signal(false);
  protected texto = '';

  protected readonly meuId = computed(() => this.autenticacao.usuarioAtual()?.id);
  protected readonly souVendedor = computed(() => this.conversa()?.vendedorId === this.meuId());
  protected readonly nomeOutraParte = computed(() => {
    const c = this.conversa();
    if (!c) return '';
    return (this.souVendedor() ? c.nomeComprador : c.nomeVendedor) ?? 'usuário removido';
  });
  protected readonly itens = computed<ItemDaConversa[]>(() => {
    const c = this.conversa();
    if (!c) return [];
    return [
      ...c.mensagens.map((mensagem) => ({ tipo: 'mensagem' as const, quando: mensagem.enviadaEm, mensagem })),
      ...c.propostas.map((proposta) => ({ tipo: 'proposta' as const, quando: proposta.enviadaEm, proposta })),
    ].sort((a, b) => a.quando.localeCompare(b.quando));
  });
  protected readonly podeProporAgora = computed(() => {
    const c = this.conversa();
    return !!c && !this.souVendedor() && c.statusAnuncio === 'ATIVO' && !c.propostas.some((p) => p.status === 'PENDENTE');
  });

  constructor() {
    this.carregar(true);
    interval(ATUALIZAR_A_CADA_MS)
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => this.carregar(false));
  }

  protected enviarMensagem(): void {
    const texto = this.texto;
    this.executar(this.negociacao.enviarMensagem(this.id, texto), () => (this.texto = ''));
  }

  protected enviarProposta(proposta: NovaProposta): void {
    this.executar(this.negociacao.enviarProposta(this.id, proposta), () => this.dialogoAberto.set(false));
  }

  protected aceitar(proposta: PropostaNegociacao): void {
    this.executar(this.negociacao.aceitar(this.id, proposta.id));
  }

  protected recusar(proposta: PropostaNegociacao): void {
    this.executar(this.negociacao.recusar(this.id, proposta.id));
  }

  protected cancelar(proposta: PropostaNegociacao): void {
    this.executar(this.negociacao.cancelar(this.id, proposta.id));
  }

  protected liberarReserva(proposta: PropostaNegociacao): void {
    if (confirm('Liberar a reserva? O anúncio volta a ficar disponível para outros compradores.')) {
      this.executar(this.negociacao.liberarReserva(this.id, proposta.id));
    }
  }

  protected pagar(proposta: PropostaNegociacao): void {
    this.router.navigate(['/marketplace/checkout'], { queryParams: { proposta: proposta.id } });
  }

  protected abrirProposta(): void {
    this.erroAcao.set(null);
    this.dialogoAberto.set(true);
  }

  protected minha(mensagem: MensagemChat): boolean {
    return mensagem.remetenteId === this.meuId();
  }

  /** Reserva vigente: aceita e ainda sem pedido (RN 10). */
  protected reservaVigente(proposta: PropostaNegociacao): boolean {
    return proposta.status === 'ACEITA' && !proposta.pedidoId;
  }

  private executar(acao: Observable<unknown>, depois?: () => void): void {
    this.ocupado.set(true);
    this.erroAcao.set(null);
    acao.subscribe({
      next: () => {
        depois?.();
        this.ocupado.set(false);
        this.carregar(false);
      },
      error: (e: ErroApi) => {
        this.erroAcao.set(e.mensagem);
        this.ocupado.set(false);
      },
    });
  }

  /** RN 11: abrir a conversa (ou receber mensagens com ela aberta) marca as recebidas como lidas. */
  private carregar(primeiraVez: boolean): void {
    this.negociacao.conversa(this.id).subscribe({
      next: (conversa) => {
        this.conversa.set(conversa);
        if (conversa.naoLidas > 0) {
          this.negociacao.marcarComoLidas(this.id).subscribe(() => this.naoLidas.atualizar());
        }
      },
      error: (e: ErroApi) => {
        if (primeiraVez) this.erro.set(e.mensagem);
      },
    });
  }
}
