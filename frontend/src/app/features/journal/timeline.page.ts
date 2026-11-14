import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Pagina } from '../../core/api/forum.service';
import { EVENTOS_PLATAFORMA, EntradaTimeline, JournalService } from '../../core/api/journal.service';
import { ErroApi } from '../../core/http/erro-api';

/** Timeline unificada do jogador (F-16): entradas manuais e automáticas, da mais recente para a mais antiga. */
@Component({
  selector: 'app-timeline',
  imports: [DatePipe, RouterLink],
  templateUrl: './timeline.page.html',
  styleUrl: './timeline.page.scss',
})
export class TimelinePage {
  private readonly journal = inject(JournalService);

  protected readonly eventos = EVENTOS_PLATAFORMA;
  protected readonly tamanho = 20;
  protected readonly pagina = signal<Pagina<EntradaTimeline> | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.carregar(1);
  }

  protected carregar(numero: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.journal.timeline(numero, this.tamanho).subscribe({
      next: (pagina) => {
        this.pagina.set(pagina);
        this.carregando.set(false);
      },
      error: (e: ErroApi) => {
        this.erro.set(e.mensagem);
        this.carregando.set(false);
      },
    });
  }
}
