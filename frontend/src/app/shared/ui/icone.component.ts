import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { ICONES, NomeIcone } from './icones';

@Component({
  selector: 'gt-icone',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<svg viewBox="0 0 24 24" aria-hidden="true" [attr.width]="tamanho()" [attr.height]="tamanho()"><path [attr.d]="caminho()" /></svg>`,
  styles: `
    :host { display: inline-flex; flex: 0 0 auto; }
    svg { fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
    :host(.preenchido) svg { fill: currentColor; }
  `,
})
export class IconeComponent {
  readonly nome = input.required<NomeIcone>();
  readonly tamanho = input(18);
  protected readonly caminho = computed(() => ICONES[this.nome()]);
}
