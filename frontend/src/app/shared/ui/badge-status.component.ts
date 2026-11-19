import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Variantes do protótipo: contornado no acento, contornado em vermelho e preenchido escuro. */
export type TipoBadge = 'acento' | 'perigo' | 'escuro' | 'neutro';

const POR_STATUS: Record<string, TipoBadge> = {
  ABERTO: 'acento', ATIVO: 'acento', PUBLICA: 'acento', RESOLVIDO: 'acento', ACEITA: 'acento',
  OCULTA: 'perigo', RECUSADA: 'perigo', CANCELADO: 'perigo', REMOVIDO: 'perigo',
  ENCERRADO: 'escuro', VENDIDO: 'escuro', CONCLUIDO: 'escuro', PAUSADO: 'escuro',
};

/** Badge de status. Sem `tipo`, a cor sai do próprio status (ABERTO, OCULTA, ENCERRADO...). */
@Component({
  selector: 'gt-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="'badge badge--' + variante()"><ng-content /></span>`,
  styles: `
    .badge { display: inline-flex; align-items: center; height: 22px; padding: 0 9px; border: 1px solid;
      border-radius: 6px; font-size: 10px; font-weight: 800; letter-spacing: 0.6px; text-transform: uppercase; white-space: nowrap; }
    .badge--acento { border-color: color-mix(in srgb, var(--accent) 55%, transparent); color: var(--accent); }
    .badge--perigo { border-color: color-mix(in srgb, var(--danger) 55%, transparent); color: var(--danger); }
    .badge--escuro { border-color: var(--line); background: var(--surface-3); color: var(--muted); }
    .badge--neutro { border-color: var(--line); color: var(--muted); }
  `,
})
export class BadgeStatusComponent {
  readonly status = input<string | null>(null);
  readonly tipo = input<TipoBadge | null>(null);
  protected readonly variante = computed(() => this.tipo() ?? POR_STATUS[(this.status() ?? '').toUpperCase()] ?? 'neutro');
}
