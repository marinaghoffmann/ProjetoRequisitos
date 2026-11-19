import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';

export interface OpcaoChip<T = string> {
  valor: T;
  rotulo: string;
}

/** Chips de filtro em pílula: o ativo tem fundo de acento. Filtram na hora (two-way em `selecionado`). */
@Component({
  selector: 'gt-chips',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="chips" role="group" [attr.aria-label]="rotulo()">
      @for (opcao of opcoes(); track opcao.valor) {
        <button type="button" class="chip" [class.chip--ativo]="opcao.valor === selecionado()"
          [attr.aria-pressed]="opcao.valor === selecionado()" (click)="selecionado.set(opcao.valor)">
          {{ opcao.rotulo }}
        </button>
      }
    </div>
  `,
  styles: `
    .chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .chip { min-height: 32px; padding: 0 14px; border: 1px solid var(--line); border-radius: 20px;
      background: var(--surface); color: var(--muted); font: 700 12px var(--font-body); }
    .chip:hover { color: var(--text); border-color: var(--faint); transform: none; filter: none; }
    .chip--ativo, .chip--ativo:hover { border-color: var(--accent); background: var(--accent); color: #0a0d10; }
  `,
})
export class ChipsComponent<T = string> {
  readonly opcoes = input.required<OpcaoChip<T>[]>();
  readonly rotulo = input('Filtros');
  readonly selecionado = model<T>();
}
