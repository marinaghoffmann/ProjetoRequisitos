import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  JogandoAgoraService, JogoEmAndamento, LIMITE_JOGANDO_AGORA, Progresso, ROTULOS_PROGRESSO,
} from '../../core/api/jogando-agora.service';
import { ErroApi } from '../../core/http/erro-api';

@Component({
  selector: 'app-jogando-agora',
  imports: [RouterLink, DatePipe],
  templateUrl: './jogando-agora.page.html',
  styleUrl: './jogando-agora.page.scss',
})
export class JogandoAgoraPage {
  private readonly servico = inject(JogandoAgoraService);

  protected readonly limite = LIMITE_JOGANDO_AGORA;
  protected readonly rotulos = ROTULOS_PROGRESSO;
  protected readonly progressos = Object.keys(ROTULOS_PROGRESSO) as Progresso[];
  protected readonly jogos = signal<JogoEmAndamento[]>([]);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.carregar();
  }

  protected alterar(id: string, progresso: Progresso): void {
    this.servico.alterarProgresso(id, progresso).subscribe({
      next: () => this.carregar(),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }

  protected remover(id: string): void {
    this.servico.remover(id).subscribe({ next: () => this.carregar() });
  }

  private carregar(): void {
    this.servico.listar().subscribe({
      next: (l) => this.jogos.set(l),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}