import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { gradientePara, imagemSegura } from '../apresentacao-jogo';

/**
 * Linha das telas da biblioteca (Em andamento, Wishlist, Zerados): capa de ~72 px, título, meta pequena,
 * chips cinza (`[chips]`) e ações à direita (`[acoes]`).
 */
@Component({
  selector: 'gt-linha-biblioteca',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="linha">
      <a class="linha__capa" [routerLink]="['/catalogo', jogoId()]" [attr.aria-label]="titulo()"
        [style.background]="capaSegura() ? null : gradiente()">
        @if (capaSegura(); as capa) { <img [src]="capa" alt="" loading="lazy" /> }
      </a>
      <div class="linha__corpo">
        <a class="linha__titulo" [routerLink]="['/catalogo', jogoId()]">{{ titulo() }}</a>
        @if (meta()) { <p class="linha__meta">{{ meta() }}</p> }
        <div class="linha__chips"><ng-content select="[chips]" /></div>
      </div>
      <div class="linha__lado"><ng-content select="[lado]" /></div>
      <div class="linha__acoes"><ng-content select="[acoes]" /></div>
    </article>
  `,
  styles: `
    .linha { display: flex; align-items: center; gap: 16px; padding: 14px; border: 1px solid var(--line-soft);
      border-radius: var(--radius); background: var(--surface); }
    .linha__capa { flex: 0 0 auto; width: 72px; aspect-ratio: 3 / 4; overflow: hidden; border-radius: 9px; background: var(--surface-2); }
    img { width: 100%; height: 100%; object-fit: cover; }
    .linha__corpo { display: flex; flex: 1; flex-direction: column; gap: 4px; min-width: 0; }
    .linha__titulo { overflow: hidden; color: var(--text); font: 700 15px var(--font-title); text-overflow: ellipsis; white-space: nowrap; }
    .linha__titulo:hover { color: var(--accent); text-decoration: none; }
    .linha__meta { margin: 0; color: var(--muted); font-size: 11.5px; }
    .linha__chips { display: flex; flex-wrap: wrap; gap: 6px; }
    .linha__chips:empty, .linha__lado:empty { display: none; }
    .linha__lado { color: var(--accent); font-weight: 800; font-size: 12px; white-space: nowrap; }
    .linha__acoes { display: flex; flex: 0 0 auto; flex-wrap: wrap; gap: 8px; align-items: center; justify-content: flex-end; }
    @media (max-width: 700px) {
      .linha { flex-wrap: wrap; }
      .linha__acoes { width: 100%; justify-content: flex-start; }
    }
  `,
})
export class LinhaBibliotecaComponent {
  readonly jogoId = input.required<string>();
  readonly titulo = input.required<string>();
  readonly capaUrl = input<string | null>(null);
  readonly meta = input<string | null>(null);

  protected capaSegura(): string | null {
    return imagemSegura(this.capaUrl());
  }

  protected gradiente(): string {
    return gradientePara(this.jogoId());
  }
}
