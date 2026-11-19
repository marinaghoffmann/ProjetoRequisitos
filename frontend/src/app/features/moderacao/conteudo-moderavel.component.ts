import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { ModeracaoService, TipoConteudo } from '../../core/api/moderacao.service';
import { ErroApi } from '../../core/http/erro-api';

/** Reusable report action and per-item, per-session 18+ reveal for public user content. */
@Component({
  selector: 'app-conteudo-moderavel',
  imports: [FormsModule, RouterLink],
  template: `
    @if (carregando()) { <p class="estado">Carregando conteúdo…</p> }
    @else if (!falha()) {
      <div class="corpo" [class.desfocado]="aviso18() && !revelado()"><ng-content /></div>
      @if (aviso18() && !revelado()) {
        <div class="aviso" role="group" aria-label="Aviso de conteúdo 18+">
          <strong>Conteúdo 18+</strong><span>Texto e imagens podem ser sensíveis.</span>
          <button type="button" (click)="revelar()">Revelar conteúdo</button>
        </div>
      }
    }
    <div class="controles">
      @if (auth.usuarioAtual()) { <button type="button" (click)="aberto.set(!aberto())">Denunciar</button> }
      @else { <a routerLink="/login">Entre para denunciar</a> }
    </div>
    @if (aberto()) {
      <form class="denuncia" (ngSubmit)="enviar()">
        <label>Por que você está denunciando este conteúdo?
          <textarea name="motivo" [(ngModel)]="motivo" required maxlength="1000" rows="3"></textarea>
        </label>
        <div><button type="submit" [disabled]="enviando() || !motivo.trim()">Enviar denúncia</button>
          <button type="button" (click)="aberto.set(false)">Cancelar</button></div>
      </form>
    }
    @if (mensagem()) { <p class="retorno" role="status">{{ mensagem() }}</p> }
  `,
  styles: `
    :host { display: block; position: relative; }
    .estado { color: var(--muted); font-size: 12px; }
    .corpo.desfocado { filter: blur(10px); pointer-events: none; user-select: none; }
    .aviso { position: absolute; inset: 12px 0 auto; z-index: 2; display: grid; justify-items: center; gap: 6px; padding: 18px; border: 1px solid var(--line); border-radius: 10px; background: var(--surface); text-align: center; }
    .aviso strong { color: var(--accent); }
    .aviso span, .controles { color: var(--muted); font-size: 12px; }
    button { cursor: pointer; }
    .controles { margin: 8px 0; }
    .controles button { border: 0; background: transparent; color: var(--muted); font-size: 11px; }
    .controles button:hover { color: var(--accent); }
    .denuncia { display: grid; gap: 10px; padding: 12px; border: 1px solid var(--line); border-radius: 10px; background: var(--surface-2); }
    .denuncia label { display: grid; gap: 6px; font-size: 12px; }
    .denuncia textarea { width: 100%; padding: 8px; border: 1px solid var(--line); border-radius: 6px; color: var(--text); background: var(--surface); }
    .denuncia div { display: flex; gap: 8px; }
    .retorno { color: var(--accent); font-size: 12px; }
  `,
})
export class ConteudoModeravelComponent implements OnInit {
  @Input({ required: true }) tipo!: TipoConteudo;
  @Input({ required: true }) conteudoId!: string;
  private readonly api = inject(ModeracaoService);
  protected readonly auth = inject(AutenticacaoService);
  protected readonly carregando = signal(true);
  protected readonly falha = signal(false);
  protected readonly aviso18 = signal(false);
  protected readonly revelado = signal(false);
  protected readonly aberto = signal(false);
  protected readonly enviando = signal(false);
  protected readonly mensagem = signal<string | null>(null);
  protected motivo = '';
  ngOnInit(): void {
    this.revelado.set(sessionStorage.getItem(this.chave()) === '1');
    this.api.estado(this.tipo, this.conteudoId).subscribe({
      next: (estado) => { this.aviso18.set(estado.aviso18); this.carregando.set(false); },
      error: () => { this.falha.set(true); this.carregando.set(false); this.mensagem.set('Não foi possível verificar o aviso de conteúdo.'); },
    });
  }
  private chave(): string { return `game-tracker.revelado.${this.tipo}.${this.conteudoId}`; }
  protected revelar(): void { sessionStorage.setItem(this.chave(), '1'); this.revelado.set(true); }
  protected enviar(): void {
    this.enviando.set(true); this.mensagem.set(null);
    this.api.denunciar(this.tipo, this.conteudoId, this.motivo).subscribe({
      next: () => { this.mensagem.set('Denúncia enviada para análise.'); this.aberto.set(false); this.enviando.set(false); },
      error: (erro: ErroApi) => { this.mensagem.set(erro.mensagem); this.enviando.set(false); },
    });
  }
}
