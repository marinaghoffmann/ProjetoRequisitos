import { CurrencyPipe } from '@angular/common';
import { Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TipoNegociacao } from '../../core/api/marketplace.service';
import { NovaProposta, TipoProposta } from '../../core/api/negociacao.service';

/**
 * Diálogo "Fazer uma proposta" (F-19 RN 6): oferta em valor ou troca por outro jogo, com uma mensagem
 * opcional. Só mostra as opções que o tipo de negociação do anúncio aceita.
 */
@Component({
  selector: 'app-proposta-dialog',
  imports: [CurrencyPipe, FormsModule],
  templateUrl: './proposta-dialog.component.html',
  styleUrl: './proposta-dialog.component.scss',
})
export class PropostaDialogComponent {
  readonly tipoNegociacao = input<TipoNegociacao | null>('AMBOS');
  readonly precoAnuncio = input<number | null>(null);
  readonly erro = input<string | null>(null);
  readonly enviando = input(false);
  readonly enviar = output<NovaProposta>();
  readonly fechar = output<void>();

  protected readonly tipo = signal<TipoProposta>('VALOR');
  protected valor: number | null = null;
  protected jogoOferecido = '';
  protected mensagem = '';

  protected aceitaValor(): boolean {
    return this.tipoNegociacao() !== 'TROCA';
  }

  protected aceitaTroca(): boolean {
    return this.tipoNegociacao() !== 'VENDA';
  }

  protected tipoEfetivo(): TipoProposta {
    if (!this.aceitaValor()) return 'TROCA';
    if (!this.aceitaTroca()) return 'VALOR';
    return this.tipo();
  }

  protected confirmar(): void {
    const tipo = this.tipoEfetivo();
    this.enviar.emit({
      tipo,
      valor: tipo === 'VALOR' ? this.valor : null,
      jogoOferecido: tipo === 'TROCA' ? this.jogoOferecido.trim() : null,
      mensagem: this.mensagem.trim() || null,
    });
  }
}
