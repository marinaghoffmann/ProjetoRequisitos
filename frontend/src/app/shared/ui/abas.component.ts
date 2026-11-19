import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';

export interface Aba<T = string> {
  valor: T;
  rotulo: string;
  contador?: number | null;
}

/** Abas em texto, com a ativa sublinhada no acento. */
@Component({
  selector: 'gt-abas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="abas" role="tablist" [attr.aria-label]="rotulo()">
      @for (aba of abas(); track aba.valor) {
        <button type="button" role="tab" class="aba" [class.aba--ativa]="aba.valor === ativa()"
          [attr.aria-selected]="aba.valor === ativa()" (click)="ativa.set(aba.valor)">
          {{ aba.rotulo }}
          @if (aba.contador !== undefined && aba.contador !== null) { <span class="aba__contador">{{ aba.contador }}</span> }
        </button>
      }
    </div>
  `,
  styles: `
    .abas { display: flex; gap: 26px; overflow-x: auto; border-bottom: 1px solid var(--line-soft); scrollbar-width: none; }
    .aba { min-height: 42px; padding: 0 2px; border: 0; border-bottom: 2px solid transparent; border-radius: 0;
      background: none; color: var(--muted); font: 700 13px var(--font-body); }
    .aba:hover { color: var(--text); transform: none; filter: none; }
    .aba--ativa { border-bottom-color: var(--accent); color: var(--text); }
    .aba__contador { color: var(--faint); font-size: 11px; }
  `,
})
export class AbasComponent<T = string> {
  readonly abas = input.required<Aba<T>[]>();
  readonly rotulo = input('Seções');
  readonly ativa = model<T>();
}
