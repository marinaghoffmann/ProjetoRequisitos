import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import {
  FORMAS_PAGAMENTO,
  FRETES,
  Pedido,
  PedidosService,
  STATUS_PAGAMENTO,
  STATUS_PEDIDO,
} from '../../core/api/pedidos.service';
import { ReputacaoService, TAGS_COMPORTAMENTO, TagComportamento } from '../../core/api/reputacao.service';
import { ErroApi } from '../../core/http/erro-api';

type Aba = 'COMPRAS' | 'VENDAS' | 'CONCLUIDOS';

/**
 * Meus pedidos (F-20): compras, vendas e concluídos, com o status da entrega e do pagamento e o saldo da
 * carteira. O vendedor registra o rastreio; o comprador confirma o recebimento; nos concluídos, as partes
 * avaliam uma à outra (F-21).
 */
@Component({
  selector: 'app-pedidos',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './pedidos.page.html',
  styleUrl: './pedidos.page.scss',
})
export class PedidosPage {
  private readonly pedidosApi = inject(PedidosService);
  private readonly reputacao = inject(ReputacaoService);

  protected readonly statusPedido = STATUS_PEDIDO;
  protected readonly statusPagamento = STATUS_PAGAMENTO;
  protected readonly fretes = FRETES;
  protected readonly formas = FORMAS_PAGAMENTO;
  protected readonly tags = TAGS_COMPORTAMENTO;
  protected readonly listaDeTags = Object.keys(TAGS_COMPORTAMENTO) as TagComportamento[];
  protected readonly escala = [1, 2, 3, 4, 5];

  protected readonly destaque = inject(ActivatedRoute).snapshot.queryParamMap.get('pedido');
  protected readonly pedidos = signal<Pedido[] | null>(null);
  protected readonly saldo = signal<number | null>(null);
  protected readonly aba = signal<Aba>('COMPRAS');
  protected readonly erro = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly ocupado = signal<string | null>(null);
  protected readonly avaliados = signal<Set<string>>(new Set());
  protected readonly avaliando = signal<string | null>(null);

  protected rastreios: Record<string, string> = {};
  protected avaliacao = { estrelas: 5, tags: new Set<TagComportamento>(), comentario: '' };

  protected readonly porAba = computed(() => {
    const todos = this.pedidos() ?? [];
    return {
      COMPRAS: todos.filter((p) => p.papel === 'COMPRA' && p.status !== 'CONCLUIDO'),
      VENDAS: todos.filter((p) => p.papel === 'VENDA' && p.status !== 'CONCLUIDO'),
      CONCLUIDOS: todos.filter((p) => p.status === 'CONCLUIDO'),
    } satisfies Record<Aba, Pedido[]>;
  });

  constructor() {
    this.carregar(true);
  }

  protected registrarEnvio(pedido: Pedido): void {
    this.executar(pedido, this.pedidosApi.registrarEnvio(pedido.id, this.rastreios[pedido.id] ?? ''));
  }

  protected confirmarRecebimento(pedido: Pedido): void {
    if (confirm('Confirmar que você recebeu o produto? O valor será liberado ao vendedor.')) {
      this.executar(pedido, this.pedidosApi.confirmarRecebimento(pedido.id));
    }
  }

  protected abrirAvaliacao(pedido: Pedido): void {
    this.avaliacao = { estrelas: 5, tags: new Set(), comentario: '' };
    this.avaliando.set(pedido.id);
  }

  protected alternarTag(tag: TagComportamento): void {
    if (this.avaliacao.tags.has(tag)) this.avaliacao.tags.delete(tag);
    else this.avaliacao.tags.add(tag);
  }

  protected avaliar(pedido: Pedido): void {
    const { estrelas, tags, comentario } = this.avaliacao;
    this.executar(
      pedido,
      this.reputacao.avaliar(pedido.id, { estrelas, tags: [...tags], comentario: comentario.trim() || null }),
      () => {
        this.avaliados.update((ids) => new Set(ids).add(pedido.id));
        this.avaliando.set(null);
      },
    );
  }

  private executar(pedido: Pedido, acao: Observable<unknown>, depois?: () => void): void {
    this.ocupado.set(pedido.id);
    this.erros.update(({ [pedido.id]: _, ...resto }) => resto);
    acao.subscribe({
      next: () => {
        depois?.();
        this.ocupado.set(null);
        this.carregar(false);
      },
      error: (e: ErroApi) => {
        this.erros.update((erros) => ({ ...erros, [pedido.id]: e.mensagem }));
        this.ocupado.set(null);
      },
    });
  }

  private carregar(primeiraVez: boolean): void {
    this.pedidosApi.saldo().subscribe({ next: (r) => this.saldo.set(r.saldo) });
    this.pedidosApi.listar().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        if (primeiraVez) this.abrirAbaDoDestaque(pedidos);
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  /** Recém-saído do checkout, o pedido novo aparece na aba dele. */
  private abrirAbaDoDestaque(pedidos: Pedido[]): void {
    const pedido = pedidos.find((p) => p.id === this.destaque);
    if (pedido) {
      this.aba.set(pedido.status === 'CONCLUIDO' ? 'CONCLUIDOS' : pedido.papel === 'COMPRA' ? 'COMPRAS' : 'VENDAS');
    }
  }
}
