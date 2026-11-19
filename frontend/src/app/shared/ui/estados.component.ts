import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { IconeComponent } from './icone.component';
import { NomeIcone } from './icones';

/** Estado vazio: ícone, título, texto e CTA (rota ou conteúdo projetado). */
@Component({
  selector: 'gt-estado-vazio',
  imports: [RouterLink, IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="vazio">
      <span class="vazio__icone"><gt-icone [nome]="icone()" [tamanho]="22" /></span>
      <h2>{{ titulo() }}</h2>
      @if (texto()) { <p>{{ texto() }}</p> }
      @if (ctaRota() && ctaRotulo()) { <a class="botao" [routerLink]="ctaRota()">{{ ctaRotulo() }}</a> }
      <ng-content />
    </section>
  `,
  styles: `
    .vazio { display: grid; justify-items: center; gap: 8px; padding: 56px 20px; border: 1px dashed var(--line);
      border-radius: var(--radius); text-align: center; }
    .vazio__icone { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 14px; background: var(--surface-2); color: var(--accent); }
    h2 { margin: 6px 0 0; font-size: 18px; }
    p { max-width: 420px; margin: 0 0 8px; color: var(--muted); font-size: 13px; }
  `,
})
export class EstadoVazioComponent {
  readonly titulo = input.required<string>();
  readonly texto = input<string>();
  readonly icone = input<NomeIcone>('vazio');
  readonly ctaRotulo = input<string>();
  readonly ctaRota = input<string | unknown[]>();
}

/** Skeleton de carregamento: grade de pôsteres ou linhas. */
@Component({
  selector: 'gt-carregando',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div [class]="'esqueleto esqueleto--' + tipo()" role="status" aria-label="Carregando">
      @for (i of itens(); track i) { <span class="esqueleto__item"></span> }
    </div>
  `,
  styles: `
    .esqueleto--cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(170px, 1fr)); gap: 20px; }
    .esqueleto--cards .esqueleto__item { aspect-ratio: 3 / 4; border-radius: 12px; }
    .esqueleto--linhas { display: grid; gap: 12px; }
    .esqueleto--linhas .esqueleto__item { height: 124px; border-radius: var(--radius); }
    .esqueleto__item { display: block; background: linear-gradient(100deg, var(--surface) 30%, var(--surface-2) 50%, var(--surface) 70%);
      background-size: 300% 100%; animation: brilho 1.4s ease-in-out infinite; }
    @keyframes brilho { from { background-position: 100% 0; } to { background-position: 0 0; } }
  `,
})
export class CarregandoComponent {
  readonly tipo = input<'cards' | 'linhas'>('cards');
  readonly quantidade = input(6);
  protected itens(): number[] {
    return Array.from({ length: this.quantidade() }, (_, i) => i);
  }
}

/** Erro com a mensagem do ErroApi. */
@Component({
  selector: 'gt-erro',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<p class="mensagem-erro" role="alert">{{ mensagem() }}</p>`,
})
export class ErroComponent {
  readonly mensagem = input.required<string>();
}
