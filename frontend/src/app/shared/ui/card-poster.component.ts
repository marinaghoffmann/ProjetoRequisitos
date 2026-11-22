import { ChangeDetectionStrategy, Component, computed, effect, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { formatarNota, juntarMeta } from '../apresentacao-jogo';
import { IconeComponent } from './icone.component';

const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps';

/** Dados mínimos do pôster; o VitrineService monta isso a partir do catálogo e da F-17. */
export interface DadosPoster {
  id: string;
  titulo: string;
  capa: string | null;
  gradiente: string;
  generos: string[];
  ano: string | null;
  plataformas: string[];
  nota: number | null;
  naWishlist?: boolean;
}

/**
 * Card de pôster: capa 3:4 (ou 16:9 com `formato="horizontal"`) com o gênero (inferior esquerdo), a nota (inferior direito) e o coração da
 * wishlist (superior direito). Abaixo, título e "ano · plataforma". Sem resumo. Conteúdo extra (por exemplo, o
 * feedback de similaridade) entra por projeção.
 */
@Component({
  selector: 'gt-card-poster',
  imports: [RouterLink, IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="poster" [class.poster--inativo]="inativo()" [class.poster--horizontal]="formato() === 'horizontal'">
      <a class="poster__capa" [routerLink]="['/catalogo', jogo().id]" [style.background]="capaSrc() ? null : jogo().gradiente"
        [attr.aria-label]="jogo().titulo">
        @if (capaSrc(); as capa) { <img [src]="capa" alt="" loading="lazy" decoding="async" /> }
        @if (selo()) { <span class="poster__selo">{{ selo() }}</span> }
        @if (jogo().generos.length) { <span class="poster__genero">{{ jogo().generos[0] }}</span> }
        <span class="poster__nota"><gt-icone nome="estrela" [tamanho]="11" class="preenchido" />{{ nota() }}</span>
      </a>
      @if (mostrarWishlist()) {
        <button type="button" class="poster__coracao" [class.poster__coracao--ativo]="jogo().naWishlist"
          [attr.aria-pressed]="!!jogo().naWishlist"
          [attr.aria-label]="(jogo().naWishlist ? 'Remover da wishlist: ' : 'Adicionar à wishlist: ') + jogo().titulo"
          (click)="alternarWishlist.emit(jogo())">
          <gt-icone nome="coracao" [tamanho]="15" [class.preenchido]="jogo().naWishlist" />
        </button>
      }
      <a class="poster__titulo" [routerLink]="['/catalogo', jogo().id]">{{ jogo().titulo }}</a>
      @if (meta()) { <p class="poster__meta">{{ meta() }}</p> }
      <ng-content />
    </article>
  `,
  styles: `
    .poster { position: relative; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
    .poster--inativo { opacity: 0.55; }
    .poster__capa { position: relative; display: block; aspect-ratio: 3 / 4; overflow: hidden; border: 1px solid var(--line-soft);
      border-radius: 12px; background: var(--surface-2); transition: transform 0.2s ease, border-color 0.2s ease; }
    .poster--horizontal .poster__capa { aspect-ratio: 16 / 9; }
    .poster__capa:hover { transform: translateY(-3px); border-color: color-mix(in srgb, var(--accent) 45%, var(--line-soft)); }
    .poster__capa::after { content: ''; position: absolute; inset: auto 0 0; height: 40%; background: linear-gradient(to top, rgb(0 0 0 / 75%), transparent); }
    img { width: 100%; height: 100%; object-fit: cover; }
    .poster__genero, .poster__nota, .poster__selo { position: absolute; z-index: 1; color: #fff; font-size: 10px; font-weight: 800; }
    .poster__genero { bottom: 9px; left: 10px; max-width: 60%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
      letter-spacing: 0.6px; text-transform: uppercase; }
    .poster__nota { right: 10px; bottom: 9px; display: flex; align-items: center; gap: 3px; color: var(--accent); }
    .poster__selo { top: 9px; left: 9px; padding: 2px 7px; border-radius: 999px; background: rgb(0 0 0 / 70%); }
    .poster__coracao { position: absolute; top: 8px; right: 8px; z-index: 2; width: 30px; min-height: 30px; height: 30px; padding: 0;
      border-radius: 50%; background: rgb(0 0 0 / 55%); color: #fff; backdrop-filter: blur(6px); }
    .poster__coracao--ativo { color: var(--danger); }
    .poster__titulo { margin-top: 6px; overflow: hidden; color: var(--text); font-size: 13px; font-weight: 800;
      text-overflow: ellipsis; white-space: nowrap; }
    .poster__titulo:hover { color: var(--accent); text-decoration: none; }
    .poster__meta { margin: 0; overflow: hidden; color: var(--muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
  `,
})
export class CardPosterComponent {
  readonly jogo = input.required<DadosPoster>();
  readonly mostrarWishlist = input(true);
  readonly inativo = input(false);
  readonly selo = input<string | null>(null);
  /** 'retrato' (3:4, padrão) ou 'horizontal' (16:9, com arte HD da Steam quando existir). */
  readonly formato = input<'retrato' | 'horizontal'>('retrato');
  readonly alternarWishlist = output<DadosPoster>();

  /** Capas horizontais HD da Steam já validadas, por id do jogo. */
  private readonly capasHd = signal<Record<string, string>>({});
  private readonly tentadas = new Set<string>();

  protected readonly capaSrc = computed(() => {
    const j = this.jogo();
    return (this.formato() === 'horizontal' && this.capasHd()[j.id]) || j.capa;
  });

  constructor() {
    effect(() => {
      if (this.formato() === 'horizontal') this.resolverCapaHd(this.jogo());
    });
  }

  protected readonly nota = computed(() => formatarNota(this.jogo().nota));
  protected readonly meta = computed(() => juntarMeta(this.jogo().ano, this.jogo().plataformas[0]));

  /** appid da Steam a partir da capa (.../apps/{appid}/...); testa capsule 616x353 e header.jpg. */
  private resolverCapaHd(j: DadosPoster): void {
    if (this.tentadas.has(j.id)) return;
    this.tentadas.add(j.id);
    const m = j.capa ? /\/apps\/(\d+)\//.exec(j.capa) : null;
    if (!m) return;
    const base = `${STEAM_CDN}/${m[1]}`;
    const candidatas = [`${base}/capsule_616x353.jpg`, `${base}/header.jpg`];
    const tentar = (i: number): void => {
      if (i >= candidatas.length) return;
      const img = new Image();
      img.onload = () => this.capasHd.update((mapa) => ({ ...mapa, [j.id]: candidatas[i] }));
      img.onerror = () => tentar(i + 1);
      img.src = candidatas[i];
    };
    tentar(0);
  }
}