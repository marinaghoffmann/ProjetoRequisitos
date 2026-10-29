import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AutenticacaoService } from '../../core/auth/autenticacao.service';
import { StatusService } from '../../core/api/status.service';
import { ErroApi } from '../../core/http/erro-api';

/** Página inicial provisória: confirma que o frontend conversa com o backend. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  templateUrl: './inicio.page.html',
  styleUrl: './inicio.page.scss',
})
export class InicioPage {
  private readonly statusService = inject(StatusService);
  protected readonly autenticacao = inject(AutenticacaoService);

  protected readonly status = signal<string | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.statusService.consultar().subscribe({
      next: (resposta) => this.status.set(resposta.status),
      error: (e: ErroApi) => this.erro.set(e.mensagem),
    });
  }
}
