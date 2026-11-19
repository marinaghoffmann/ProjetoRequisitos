import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Cabeçalho padrão das páginas: eyebrow opcional (com o traço de 18 px), título em Space Grotesk, subtítulo
 * e, à direita, a ação primária projetada em `[acao]`.
 */
@Component({
  selector: 'gt-cabecalho-pagina',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="cp">
      <div class="cp__textos">
        @if (eyebrow()) { <p class="cp__eyebrow">{{ eyebrow() }}</p> }
        <h1 class="cp__titulo">{{ titulo() }}</h1>
        @if (subtitulo()) { <p class="cp__subtitulo">{{ subtitulo() }}</p> }
        <ng-content />
      </div>
      <div class="cp__acao"><ng-content select="[acao]" /></div>
    </header>
  `,
  styles: `
    .cp { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 28px; }
    .cp__textos { min-width: 0; }
    .cp__eyebrow { display: flex; align-items: center; gap: 8px; margin: 0 0 10px; color: var(--accent);
      font-size: 10px; font-weight: 800; letter-spacing: 1.4px; text-transform: uppercase; }
    .cp__eyebrow::before { content: ''; width: 18px; height: 2px; background: currentColor; }
    .cp__titulo { margin: 0; font: 700 clamp(28px, 3.4vw, 44px) / 1.02 var(--font-title); letter-spacing: -1.8px; }
    .cp__subtitulo { margin: 8px 0 0; color: var(--muted); font-size: 12px; }
    .cp__acao { display: flex; flex: 0 0 auto; gap: 8px; align-items: center; }
    .cp__acao:empty { display: none; }
    @media (max-width: 700px) { .cp { flex-direction: column; align-items: flex-start; } }
  `,
})
export class CabecalhoPaginaComponent {
  readonly titulo = input.required<string>();
  readonly eyebrow = input<string>();
  readonly subtitulo = input<string>();
}
