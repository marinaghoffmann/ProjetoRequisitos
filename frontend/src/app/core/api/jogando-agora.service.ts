import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type Progresso = 'NO_INICIO' | 'NA_METADE' | 'PERTO_DO_FIM';

export const ROTULOS_PROGRESSO: Record<Progresso, string> = {
  NO_INICIO: 'No início',
  NA_METADE: 'Na metade',
  PERTO_DO_FIM: 'Perto do fim',
};

export const LIMITE_JOGANDO_AGORA = 5;

export interface JogoEmAndamento {
  jogoId: string;
  titulo: string;
  capaUrl: string | null;
  progresso: Progresso;
  dataInicio: string;
}

@Injectable({ providedIn: 'root' })
export class JogandoAgoraService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/biblioteca/jogando-agora';

  listar() {
    return this.http.get<JogoEmAndamento[]>(this.base);
  }

  marcar(jogoId: string, substituirJogoId?: string) {
    return this.http.post<void>(this.base, { jogoId, substituirJogoId: substituirJogoId ?? null });
  }

  alterarProgresso(jogoId: string, progresso: Progresso) {
    return this.http.patch<void>(`${this.base}/${jogoId}`, { progresso });
  }

  remover(jogoId: string) {
    return this.http.delete<void>(`${this.base}/${jogoId}`);
  }
}