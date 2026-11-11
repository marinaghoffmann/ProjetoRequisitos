import { Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, merge, timer } from 'rxjs';

import { NaoLidasService } from './core/api/nao-lidas.service';
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
  private readonly contador = inject(NaoLidasService);

  /** Total de mensagens recebidas e não lidas, atualizado periodicamente, a cada navegação e ao ler uma conversa. */
  protected readonly naoLidas = this.contador.total;

  constructor() {
    const navegacoes = inject(Router).events.pipe(filter((evento) => evento instanceof NavigationEnd));
    merge(timer(0, CONTAR_NAO_LIDAS_A_CADA_MS), navegacoes)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.contador.atualizar());
  }
}
