import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';

import { AutenticacaoService } from '../auth/autenticacao.service';

export interface ResumoBiblioteca {
  emAndamento: number;
  naWishlist: number;
  jogosZerados: number;
}

const VAZIO: ResumoBiblioteca = { emAndamento: 0, naWishlist: 0, jogosZerados: 0 };

/** Totais da "Minha biblioteca" no menu. As telas que mudam a biblioteca chamam atualizar(). */
@Injectable({ providedIn: 'root' })
export class ResumoBibliotecaService {
  private readonly http = inject(HttpClient);
  private readonly autenticacao = inject(AutenticacaoService);

  readonly resumo = signal<ResumoBiblioteca>(VAZIO);

  atualizar(): void {
    if (!this.autenticacao.usuarioAtual()) {
      this.resumo.set(VAZIO);
      return;
    }
    this.http
      .get<ResumoBiblioteca>('/api/perfis/me/resumo')
      .pipe(catchError(() => of(VAZIO)))
      .subscribe((resumo) => this.resumo.set(resumo));
  }
}
