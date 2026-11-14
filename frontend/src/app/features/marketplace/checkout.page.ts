import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import {
  CartaoSimulado,
  EnderecoEntrega,
  FORMAS_PAGAMENTO,
  FRETES,
  FormaPagamento,
  OpcaoFrete,
  PedidosService,
  ResumoCheckout,
} from '../../core/api/pedidos.service';
import { ErroApi } from '../../core/http/erro-api';
import { urlDaImagem } from './imagem';

/**
 * Checkout da F-20 em dois passos: endereço e frete; depois forma de pagamento, total e aviso de custódia.
 * Vem da proposta aceita no chat (?proposta=) ou da compra direta no anúncio (?anuncio=). As regras são
 * conferidas pelo backend; a tela só antecipa o óbvio.
 */
@Component({
  selector: 'app-checkout',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './checkout.page.html',
  styleUrl: './checkout.page.scss',
})
export class CheckoutPage {
  private readonly pedidos = inject(PedidosService);
  private readonly router = inject(Router);
  private readonly parametros = inject(ActivatedRoute).snapshot.queryParamMap;

  protected readonly fretes = FRETES;
  protected readonly formas = FORMAS_PAGAMENTO;
  protected readonly formasDisponiveis = Object.keys(FORMAS_PAGAMENTO) as FormaPagamento[];
  protected readonly imagem = urlDaImagem;

  protected readonly resumo = signal<ResumoCheckout | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroPedido = signal<string | null>(null);
  protected readonly passo = signal<1 | 2>(1);
  protected readonly enviando = signal(false);
  protected readonly frete = signal<OpcaoFrete>('ENVIO_SEGURO');
  protected readonly forma = signal<FormaPagamento>('PIX');

  protected endereco: EnderecoEntrega = {
    destinatario: '',
    cep: '',
    rua: '',
    numero: '',
    complemento: '',
    cidade: '',
    uf: '',
  };
  protected cartao: CartaoSimulado = { numero: '', nomeImpresso: '', validade: '', cvv: '' };

  protected readonly valorFrete = computed(
    () => this.resumo()?.fretes.find((f) => f.opcao === this.frete())?.valor ?? 0,
  );
  /** RN 4: item mais frete, em duas casas (a conta final é do backend). */
  protected readonly total = computed(
    () => Math.round(((this.resumo()?.valorItem ?? 0) + this.valorFrete()) * 100) / 100,
  );
  protected readonly saldoInsuficiente = computed(
    () => this.forma() === 'SALDO_CARTEIRA' && (this.resumo()?.saldoCarteira ?? 0) < this.total(),
  );

  constructor() {
    this.pedidos
      .checkout({ anuncioId: this.parametros.get('anuncio'), propostaId: this.parametros.get('proposta') })
      .subscribe({
        next: (resumo) => this.resumo.set(resumo),
        error: (e: ErroApi) => this.erro.set(e.mensagem),
      });
  }

  /** Formata o CEP enquanto o usuário digita: 00000-000. */
  protected formatarCep(valor: string): void {
    const digitos = valor.replace(/\D/g, '').slice(0, 8);
    this.endereco.cep = digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
  }

  /** Formata a validade enquanto o usuário digita: MM/AA. */
  protected formatarValidade(valor: string): void {
    const digitos = valor.replace(/\D/g, '').slice(0, 4);
    this.cartao.validade = digitos.length > 2 ? `${digitos.slice(0, 2)}/${digitos.slice(2)}` : digitos;
  }

  protected irParaPagamento(): void {
    this.erroPedido.set(null);
    this.passo.set(2);
  }

  protected confirmar(): void {
    const resumo = this.resumo();
    if (!resumo) return;
    this.enviando.set(true);
    this.erroPedido.set(null);
    this.pedidos
      .gerar({
        anuncioId: resumo.origem === 'COMPRA_DIRETA' ? resumo.anuncioId : null,
        propostaId: resumo.propostaId,
        endereco: { ...this.endereco, complemento: this.endereco.complemento?.trim() || null },
        frete: this.frete(),
        formaPagamento: this.forma(),
        cartao: this.forma() === 'CARTAO' ? this.cartao : null,
      })
      .subscribe({
        next: (pedido) => this.router.navigate(['/marketplace/pedidos'], { queryParams: { pedido: pedido.id } }),
        error: (e: ErroApi) => {
          this.erroPedido.set(e.mensagem);
          this.enviando.set(false);
        },
      });
  }
}
