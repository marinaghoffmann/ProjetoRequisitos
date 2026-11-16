import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CATEGORIAS_REVIEW, CategoriaReview, Review, ReviewsService } from '../../core/api/reviews.service';
import { ErroApi } from '../../core/http/erro-api';

const ROTULOS = Object.fromEntries(CATEGORIAS_REVIEW.map((c) => [c.chave, c.rotulo])) as Record<
  CategoriaReview,
  string
>;

/** Aba "Reviews" do perfil (F-02): as reviews do usuário logado, da mais recente para a mais antiga. */
@Component({
  selector: 'app-perfil-reviews',
  imports: [DatePipe, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="pf-secao">
      <div class="pf-secao__cab">
        <div>
          <h2>Minhas reviews</h2>
          <p>{{ reviews().length }} {{ reviews().length === 1 ? 'jogo avaliado' : 'jogos avaliados' }}</p>
        </div>
      </div>

      @if (erro(); as mensagem) {
        <p class="pf-aviso" role="alert">{{ mensagem }}</p>
      }

      @if (carregando()) {
        <p class="pf-estado">Carregando…</p>
      } @else {
        @for (r of reviews(); track r.id) {
          <article class="pr-review">
            <a class="pr-capa" [routerLink]="['/catalogo', r.jogoId]" [attr.aria-label]="'Abrir ' + (r.jogoTitulo ?? 'jogo')">
              @if (r.jogoCapaUrl) { <img [src]="r.jogoCapaUrl" alt="" loading="lazy" /> }
            </a>
            <div class="pr-corpo">
              <header>
                <a class="pr-titulo" [routerLink]="['/catalogo', r.jogoId]">{{ r.jogoTitulo ?? 'Jogo' }}</a>
                <span class="pr-estrelas" [attr.aria-label]="'Nota ' + r.notaGeral + ' de 5'">{{ estrelas(r.notaGeral) }}</span>
                <time [attr.datetime]="r.criadaEm">{{ r.criadaEm | date: 'dd/MM/yyyy' }}</time>
                @if (r.editada) { <small>(editada)</small> }
              </header>
              @if (r.notasPorCategoria.length) {
                <ul class="pr-notas">
                  @for (n of r.notasPorCategoria; track n.categoria) {
                    <li>{{ rotulos[n.categoria] }} {{ n.nota }}</li>
                  }
                </ul>
              }
              @if (r.texto) { <p class="pr-texto">{{ r.texto }}</p> }
            </div>
          </article>
        } @empty {
          <p class="pf-estado">
            Você ainda não avaliou nenhum jogo.
            <a routerLink="/catalogo">Explorar o catálogo</a>
          </p>
        }
      }
    </section>
  `,
  styles: `
    /* Mesmo visual das seções do perfil (os estilos de perfil.page.scss não alcançam este componente). */
    :host { display: block; }
    .pf-secao { padding: 28px 0 32px; }
    .pf-secao__cab { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 18px; }
    .pf-secao__cab h2 { margin: 0; font: 700 18px var(--font-title); letter-spacing: -0.02em; }
    .pf-secao__cab p { margin: 4px 0 0; color: var(--muted); font-size: 11px; }
    .pf-estado { padding: 12px 0; color: var(--muted); font-size: 12px; }
    .pf-estado a { margin-left: 6px; font-weight: 700; }
    .pf-aviso { margin-bottom: 14px; padding: 10px 14px; border: 1px solid rgb(255 116 72 / 25%); border-radius: 9px; background: rgb(255 116 72 / 7%); color: #ffc0aa; font-size: 12px; }
    .pr-review { display: flex; gap: 16px; padding: 16px 0; border-top: 1px solid var(--line-soft); }
    .pr-capa { flex: none; width: 64px; height: 86px; overflow: hidden; border-radius: 8px; background: var(--surface-2); }
    .pr-capa img { width: 100%; height: 100%; object-fit: cover; }
    .pr-corpo { min-width: 0; }
    .pr-corpo header { display: flex; flex-wrap: wrap; align-items: baseline; gap: 10px; }
    .pr-titulo { color: var(--text); font-weight: 800; }
    .pr-titulo:hover { color: var(--accent); text-decoration: none; }
    .pr-estrelas { color: var(--accent); letter-spacing: 0.1em; }
    time { color: var(--muted); font-size: 11px; }
    .pr-notas { display: flex; flex-wrap: wrap; gap: 6px; margin: 8px 0 0; padding: 0; list-style: none; }
    .pr-notas li { padding: 2px 8px; border: 1px solid var(--line); border-radius: 999px; color: var(--muted); font-size: 10px; }
    .pr-texto { margin: 8px 0 0; font-size: 12px; white-space: pre-line; }
  `,
})
export class PerfilReviewsComponent implements OnInit {
  private readonly servico = inject(ReviewsService);

  protected readonly rotulos = ROTULOS;
  protected readonly reviews = signal<Review[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.servico.minhas().subscribe({
      next: (reviews) => {
        this.reviews.set(reviews);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }

  protected estrelas(nota: number): string {
    return '★'.repeat(nota) + '☆'.repeat(5 - nota);
  }
}
