import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { AvisosService } from './avisos.service';
import { ModalComponent } from './modal.component';

/** Hospeda, no shell, os toasts e o modal de confirmação do AvisosService. */
@Component({
  selector: 'gt-avisos',
  imports: [ModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of avisos.toasts(); track toast.id) {
        <p class="toast" [class.toast--erro]="toast.tipo === 'erro'" (click)="avisos.fecharToast(toast.id)">{{ toast.texto }}</p>
      }
    </div>
    @if (avisos.confirmacao(); as pedido) {
      <gt-modal [titulo]="pedido.titulo" [largura]="420" (fechar)="pedido.responder(false)">
        <p class="confirmacao__texto">{{ pedido.mensagem }}</p>
        <div class="confirmacao__botoes">
          <button type="button" class="botao--secundario" (click)="pedido.responder(false)">Cancelar</button>
          <button type="button" [class.perigo]="pedido.perigo" (click)="pedido.responder(true)">{{ pedido.confirmar ?? 'Confirmar' }}</button>
        </div>
      </gt-modal>
    }
  `,
  styles: `
    .toasts { position: fixed; right: 20px; bottom: 20px; z-index: 90; display: grid; gap: 8px; max-width: min(360px, calc(100vw - 32px)); }
    .toast { margin: 0; padding: 12px 16px; border: 1px solid color-mix(in srgb, var(--accent) 40%, var(--line));
      border-left: 3px solid var(--accent); border-radius: 10px; background: var(--surface-2); box-shadow: var(--shadow);
      color: var(--text); font-size: 13px; font-weight: 600; cursor: pointer; animation: entrar 0.2s ease; }
    .toast--erro { border-color: color-mix(in srgb, var(--danger) 40%, var(--line)); border-left-color: var(--danger); }
    .confirmacao__texto { margin: 0 0 20px; color: var(--muted); }
    .confirmacao__botoes { display: flex; justify-content: flex-end; gap: 8px; }
    .perigo { background: var(--danger); color: #fff; }
    @keyframes entrar { from { opacity: 0; transform: translateY(6px); } }
    @media (max-width: 700px) { .toasts { bottom: 84px; } }
  `,
})
export class AvisosComponent {
  protected readonly avisos = inject(AvisosService);
}
