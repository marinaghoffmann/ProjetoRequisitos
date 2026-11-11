import { Injectable, inject, signal } from '@angular/core';
import { catchError, map, of } from 'rxjs';

import { AutenticacaoService } from '../auth/autenticacao.service';
import { NegociacaoService } from './negociacao.service';

/** Total de mensagens recebidas e não lidas (F-19 RN 11), mostrado no menu e atualizado por quem lê. */
@Injectable({ providedIn: 'root' })
export class NaoLidasService {
  private readonly negociacao = inject(NegociacaoService);
  private readonly autenticacao = inject(AutenticacaoService);

  readonly total = signal(0);

  atualizar(): void {
    if (!this.autenticacao.usuarioAtual()) {
      this.total.set(0);
      return;
    }
    this.negociacao
      .naoLidas()
      .pipe(map((r) => r.total), catchError(() => of(0)))
      .subscribe((total) => this.total.set(total));
  }
}
