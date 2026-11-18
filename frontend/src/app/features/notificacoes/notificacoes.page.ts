import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Notificacao, NotificacoesService } from '../../core/api/notificacoes.service';
import { ErroApi } from '../../core/http/erro-api';
@Component({
  selector: 'app-notificacoes',
  imports: [RouterLink],
  template: `
    <main class="pagina"><a routerLink="/lancamentos">← Lançamentos</a><h1>Notificações</h1>
      @if (erro()) { <p role="alert">{{ erro() }}</p> }
      @if (carregando()) { <p>Carregando…</p> }
      @else if (!notificacoes().length) { <p>Você ainda não tem notificações.</p> }
      @else {
        <ul>@for (n of notificacoes(); track n.id) {
          <li [class.lida]="n.lidaEm"><strong>{{ n.titulo }}</strong><p>{{ n.mensagem }}</p>
            @if (!n.lidaEm) { <button type="button" (click)="marcarLida(n)">Marcar como lida</button> }
          </li>
        }</ul>
      }
    </main>
  `,
  styles: [`
    .pagina{max-width:760px;margin:auto;padding:32px}ul{list-style:none;padding:0}
    li{background:#171c28;border:1px solid #596277;border-radius:12px;padding:16px;margin-bottom:12px}
    li.lida{opacity:.65}a{color:#b7a8ff}button{padding:8px;background:#7c5cff;color:white;border:0;border-radius:8px}
  `],
})
export class NotificacoesPage {
  private readonly api = inject(NotificacoesService);
  protected readonly notificacoes = signal<Notificacao[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  constructor() { this.api.listar().subscribe({
    next: n => { this.notificacoes.set(n); this.carregando.set(false); },
    error: (e: ErroApi) => { this.erro.set(e.mensagem); this.carregando.set(false); },
  }); }
  protected marcarLida(n: Notificacao): void {
    this.api.marcarLida(n.id).subscribe({
      next: atual => this.notificacoes.update(lista => lista.map(item => item.id === atual.id ? atual : item)),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
