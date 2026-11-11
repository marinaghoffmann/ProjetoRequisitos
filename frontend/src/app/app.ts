import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, filter, map, merge, of, switchMap, timer } from 'rxjs';

import { NegociacaoService } from './core/api/negociacao.service';
import { AutenticacaoService } from './core/auth/autenticacao.service';

/** Intervalo da contagem de mensagens não lidas do menu (F-19 RN 11). */
const CONTAR_NAO_LIDAS_A_CADA_MS = 30000;

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly autenticacao = inject(AutenticacaoService);
  private readonly negociacao = inject(NegociacaoService);

  /** Total de mensagens recebidas e não lidas, atualizado periodicamente e a cada navegação. */
  protected readonly naoLidas = signal(0);

  constructor() {
    const navegacoes = inject(Router).events.pipe(filter((evento) => evento instanceof NavigationEnd));
    merge(timer(0, CONTAR_NAO_LIDAS_A_CADA_MS), navegacoes)
      .pipe(
        switchMap(() => this.autenticacao.usuarioAtual()
          ? this.negociacao.naoLidas().pipe(map((r) => r.total), catchError(() => of(0)))
          : of(0)),
        takeUntilDestroyed(),
      )
      .subscribe((total) => this.naoLidas.set(total));
  }
}
