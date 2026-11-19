import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { IconeComponent } from './icone.component';

/** Modal centralizado com fundo escurecido. Fecha pelo X, pelo fundo ou pelo Esc. */
@Component({
  selector: 'gt-modal',
  imports: [IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'fechar.emit()' },
  template: `
    <div class="modal__fundo" (click)="fechar.emit()"></div>
    <section class="modal" role="dialog" aria-modal="true" [attr.aria-label]="titulo()" [style.max-width.px]="largura()">
      <header class="modal__topo">
        <h2>{{ titulo() }}</h2>
        <button type="button" class="modal__fechar" aria-label="Fechar" (click)="fechar.emit()"><gt-icone nome="fechar" /></button>
      </header>
      <div class="modal__corpo"><ng-content /></div>
    </section>
  `,
  styles: `
    :host { position: fixed; inset: 0; z-index: 80; display: grid; place-items: center; padding: 16px; }
    .modal__fundo { position: absolute; inset: 0; background: rgb(3 4 6 / 72%); backdrop-filter: blur(3px); }
    .modal { position: relative; width: 100%; max-height: calc(100vh - 32px); overflow-y: auto; padding: 22px 24px 24px;
      border: 1px solid var(--line); border-radius: 16px; background: var(--surface); box-shadow: var(--shadow); }
    .modal__topo { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
    h2 { margin: 0; font-size: 20px; }
    .modal__fechar { width: 32px; min-height: 32px; height: 32px; padding: 0; border-radius: 8px; background: var(--surface-2); color: var(--muted); }
    .modal__fechar:hover { color: var(--text); transform: none; }
  `,
})
export class ModalComponent {
  readonly titulo = input.required<string>();
  readonly largura = input(560);
  readonly fechar = output<void>();
}
