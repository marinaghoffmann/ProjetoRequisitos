import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  CATEGORIAS_REVIEW, CategoriaReview, LIMITE_TEXTO_REVIEW, Review, ReviewsDoJogo, ReviewsService,
} from '../../core/api/reviews.service';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ErroApi } from '../../core/http/erro-api';

type NotasDoFormulario = Record<CategoriaReview, number | null>;

const TAMANHO_DA_PAGINA = 10;

function notasVazias(): NotasDoFormulario {
  return { GRAFICOS: null, SOM: null, JOGABILIDADE: null, DIVERSAO: null, INOVACAO: null };
}

import { ConteudoModeravelComponent } from '../moderacao/conteudo-moderavel.component';

/**
 * Reviews do jogo (F-02): médias geral e por categoria, lista paginada e o modal para escrever ou editar
 * a review do usuário logado. RN 1: quem já avaliou o jogo abre o modal em modo de edição. RN 2: só o
 * autor vê Editar e Excluir (o backend confere de novo).
 */
@Component({
  selector: 'app-reviews-jogo',
  imports: [ConteudoModeravelComponent, DatePipe, RouterLink],
  templateUrl: './reviews-jogo.component.html',
  styleUrl: './reviews-jogo.component.scss',
})
export class ReviewsJogoComponent implements OnInit {
  private readonly reviews = inject(ReviewsService);
  protected readonly autenticacao = inject(AutenticacaoService);

  readonly jogoId = input.required<string>();

  protected readonly categorias = CATEGORIAS_REVIEW;
  protected readonly estrelas = [1, 2, 3, 4, 5];
  protected readonly limiteTexto = LIMITE_TEXTO_REVIEW;

  protected readonly dados = signal<ReviewsDoJogo | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly pagina = signal(1);
  /** A review do usuário logado para este jogo, se ele já avaliou. */
  protected readonly minha = signal<Review | null>(null);
  protected readonly confirmandoExclusao = signal<string | null>(null);

  // ===== Modal =====
  protected readonly modalAberto = signal(false);
  /** null: nova review; id: edição da review existente. */
  protected readonly editandoId = signal<string | null>(null);
  protected readonly notaGeral = signal<number | null>(null);
  protected readonly notas = signal<NotasDoFormulario>(notasVazias());
  protected readonly texto = signal('');
  protected readonly salvando = signal(false);
  protected readonly erroModal = signal<string | null>(null);
  protected readonly avisoModal = signal<string | null>(null);

  protected readonly rotulos = computed(() =>
    Object.fromEntries(this.categorias.map((c) => [c.chave, c.rotulo])) as Record<CategoriaReview, string>,
  );

  ngOnInit(): void {
    this.carregar();
    this.carregarMinha();
  }

  // ===== Consulta =====

  protected irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.carregar();
  }

  protected ehMinha(review: Review): boolean {
    return review.autorId === this.autenticacao.usuarioAtual()?.id;
  }

  /** Nota com duas casas e vírgula; sem notas vira um traço. */
  protected media(valor: number | null): string {
    return valor === null ? '–' : valor.toFixed(2).replace('.', ',');
  }

  protected estrelasDe(nota: number): string {
    return '★'.repeat(nota) + '☆'.repeat(5 - nota);
  }

  // ===== Escrever / editar =====

  protected abrir(): void {
    const existente = this.minha();
    if (existente) {
      this.editar(existente);
      this.avisoModal.set('Você já avaliou este jogo: altere as notas ou o texto da sua review.');
      return;
    }
    this.preencher(null);
    this.modalAberto.set(true);
  }

  protected editar(review: Review): void {
    this.preencher(review);
    this.modalAberto.set(true);
  }

  protected fechar(): void {
    this.modalAberto.set(false);
  }

  protected escolherNotaGeral(nota: number): void {
    this.notaGeral.set(nota);
  }

  protected escolherNota(categoria: CategoriaReview, valor: string): void {
    this.notas.update((atuais) => ({ ...atuais, [categoria]: valor ? Number(valor) : null }));
  }

  protected salvar(): void {
    const notaGeral = this.notaGeral();
    if (notaGeral === null) {
      this.erroModal.set('Escolha a nota geral de 1 a 5.');
      return;
    }
    const dados = {
      jogoId: this.jogoId(),
      notaGeral,
      notasPorCategoria: this.categorias
        .filter((c) => this.notas()[c.chave] !== null)
        .map((c) => ({ categoria: c.chave, nota: this.notas()[c.chave] as number })),
      texto: this.texto(),
    };
    const id = this.editandoId();
    this.salvando.set(true);
    this.erroModal.set(null);
    (id ? this.reviews.editar(id, dados) : this.reviews.publicar(dados)).subscribe({
      next: (review) => {
        this.salvando.set(false);
        this.minha.set(review);
        this.modalAberto.set(false);
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.salvando.set(false);
        if (e.status === 409) {
          this.abrirEdicaoDaExistente();
          return;
        }
        this.erroModal.set(e.mensagem);
      },
    });
  }

  protected excluir(review: Review): void {
    this.reviews.excluir(review.id).subscribe({
      next: () => {
        this.confirmandoExclusao.set(null);
        if (this.minha()?.id === review.id) this.minha.set(null);
        this.carregar();
      },
      error: (e: ErroApi) => {
        this.confirmandoExclusao.set(null);
        this.erro.set(e.mensagem);
      },
    });
  }

  // ===== Apoio =====

  private carregar(): void {
    this.reviews.doJogo(this.jogoId(), this.pagina(), TAMANHO_DA_PAGINA).subscribe({
      next: (dados) => {
        this.dados.set(dados);
        this.erro.set(null);
      },
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  private carregarMinha(): void {
    if (!this.autenticacao.usuarioAtual()) return;
    this.reviews.minhaDoJogo(this.jogoId()).subscribe({ next: (review) => this.minha.set(review) });
  }

  /** RN 1: o jogo já tinha review do usuário (por exemplo, publicada em outra aba); abre a edição dela. */
  private abrirEdicaoDaExistente(): void {
    this.reviews.minhaDoJogo(this.jogoId()).subscribe({
      next: (review) => {
        if (!review) return;
        this.minha.set(review);
        this.preencher(review);
        this.avisoModal.set('Você já avaliou este jogo: altere as notas ou o texto da sua review.');
      },
    });
  }

  private preencher(review: Review | null): void {
    const notas = notasVazias();
    review?.notasPorCategoria.forEach((n) => (notas[n.categoria] = n.nota));
    this.editandoId.set(review?.id ?? null);
    this.notaGeral.set(review?.notaGeral ?? null);
    this.notas.set(notas);
    this.texto.set(review?.texto ?? '');
    this.erroModal.set(null);
    this.avisoModal.set(null);
  }
}
